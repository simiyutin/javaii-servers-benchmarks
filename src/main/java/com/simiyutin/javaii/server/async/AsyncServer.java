package com.simiyutin.javaii.server.async;

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

public class AsyncServer extends Server {
    private final AsynchronousServerSocketChannel serverSocket;

    public AsyncServer(int port) throws IOException {
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
                SortAlgorithm.sort(reader.constructedMessage);
                reader.dump(buffer);
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
        List<Integer> constructedMessage = new ArrayList<>();
        int curSize = -1;

        boolean finished() {
            return constructedMessage.size() == curSize;
        }

        void feed(ByteBuffer buffer) {
            buffer.flip();

            while (buffer.remaining() >= 4) {
                int readValue = buffer.getInt();
                if (curSize == -1) {
                    curSize = readValue;
                } else if (constructedMessage.size() == curSize) {
                    throw new AssertionError("lolwut");
                } else {
                    constructedMessage.add(readValue);
                }
            }

            buffer.compact();
        }

        void dump(ByteBuffer buffer) {
            buffer.clear();
            buffer.putInt(curSize);
            for (Integer val : constructedMessage) {
                buffer.putInt(val);
            }
        }
    }
}
