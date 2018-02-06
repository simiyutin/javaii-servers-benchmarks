package com.simiyutin.javaii.server.tcp_async;

import com.google.protobuf.InvalidProtocolBufferException;
import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.SortAlgorithm;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TCPAsyncServer extends Server {
    private final AsynchronousServerSocketChannel serverSocket;

    public TCPAsyncServer(int port) throws IOException {
        this.serverSocket = AsynchronousServerSocketChannel.open();
        this.serverSocket.bind(new InetSocketAddress(port));
    }

    @Override
    public void start() throws IOException {
        while (true) {
            try {
                Future<AsynchronousSocketChannel> socketFuture = serverSocket.accept();
                AsynchronousSocketChannel socket = socketFuture.get();
                ByteBuffer buffer = ByteBuffer.allocate(1024); //todo what if larger array?
                Reader reader = new Reader();
                Attachment attachment = new Attachment();
                attachment.buffer = buffer;
                attachment.socket = socket;
                attachment.reader = reader;
                socket.read(buffer, attachment, new ReaderHandler());
            } catch (InterruptedException | ExecutionException e) {
                e.printStackTrace();
            }
        }
    }

    private static class Attachment {
        ByteBuffer buffer;
        AsynchronousSocketChannel socket;
        Reader reader;
    }

    private static class ReaderHandler implements CompletionHandler<Integer, Attachment> {

        @Override
        public void completed(Integer result, Attachment attachment) {
            Reader reader = attachment.reader;
            ByteBuffer buffer = attachment.buffer;
            AsynchronousSocketChannel socket = attachment.socket;
            reader.feed(buffer);
            if (!reader.finished()) {
                socket.read(buffer, attachment, this);
            } else {
                MessageProtos.Message message = reader.parseMessage();
                List<Integer> array = new ArrayList<>(message.getArrayList());
                SortAlgorithm.sort(array);
                MessageProtos.Message response = MessageProtos.Message.newBuilder().addAllArray(array).build();
                byte[] bytes = response.toByteArray();
                buffer.putInt(bytes.length);
                buffer.put(bytes);
                buffer.flip();
                attachment.reader = new Reader();
                socket.write(buffer, attachment, new WriterHandler());
            }
        }

        @Override
        public void failed(Throwable exc, Attachment attachment) {

        }
    }

    public static class WriterHandler implements CompletionHandler<Integer, Attachment> {
        @Override
        public void completed(Integer result, Attachment attachment) {
            AsynchronousSocketChannel socket = attachment.socket;
            ByteBuffer buffer = attachment.buffer;

            if (result > 0) {
                socket.write(buffer, attachment, this);
            } else {
                buffer.clear();
                socket.read(buffer, attachment, new ReaderHandler());
            }
        }

        @Override
        public void failed(Throwable exc, Attachment attachment) {

        }
    }

    // должен считать ровно один массив
    private static class Reader {
        private byte[] data = null;
        private int offset = 0;

        boolean finished() {
            return data != null && data.length == offset;
        }

        MessageProtos.Message parseMessage() {
            MessageProtos.Message result = MessageProtos.Message.getDefaultInstance();
            try {
                result = MessageProtos.Message.parseFrom(data);
            } catch (InvalidProtocolBufferException e) {
                e.printStackTrace();
            }
            return result;
        }

        void feed(ByteBuffer buffer) {
            buffer.flip();
            while (buffer.remaining() > 0) {
                if (data == null && buffer.remaining() < 4) {
                    break;
                }

                if (data == null) {
                    int byteSize = buffer.getInt();
                    data = new byte[byteSize];
                } else if (offset == data.length) {
                    throw new AssertionError("lolwut");
                } else {
                    int remaining = buffer.remaining();
                    buffer.get(data, offset, remaining);
                    offset += remaining;
                }
            }

            buffer.compact();
        }
    }
}
