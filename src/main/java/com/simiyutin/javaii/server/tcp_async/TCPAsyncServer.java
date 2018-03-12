package com.simiyutin.javaii.server.tcp_async;

import com.google.protobuf.InvalidProtocolBufferException;
import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.SortAlgorithm;
import com.simiyutin.javaii.statistics.ServeStatistic;
import com.simiyutin.javaii.statistics.SortStatistic;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.AsynchronousServerSocketChannel;
import java.nio.channels.AsynchronousSocketChannel;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.CompletionHandler;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class TCPAsyncServer extends Server {
    private AsynchronousServerSocketChannel serverSocket;
    private Thread listener;
    private final int port;

    public TCPAsyncServer(int port) {
        this.port = port;
    }

    @Override
    public void start() throws IOException {
        this.serverSocket = AsynchronousServerSocketChannel.open();
        this.serverSocket.bind(new InetSocketAddress(port));
        listener = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    Future<AsynchronousSocketChannel> socketFuture = serverSocket.accept();
                    AsynchronousSocketChannel socket = socketFuture.get();
                    ByteBuffer buffer = ByteBuffer.allocate(1024);
                    Reader reader = new Reader();
                    Attachment attachment = new Attachment();
                    attachment.buffer = buffer;
                    attachment.socket = socket;
                    attachment.reader = reader;
                    socket.read(buffer, attachment, new ReaderHandler());
                }
                catch (InterruptedException e) {
                    return;
                }
                catch (ExecutionException e) {
                    if (e.getCause() != null && e.getCause() instanceof ClosedChannelException) {
                        return;
                    }
                    e.printStackTrace();
                }
            }
        });
        listener.start();
    }

    @Override
    public void stop() {
        if (listener != null) {
            listener.interrupt();
        }
        try {
            serverSocket.close();
        } catch (IOException ignored) {}
    }

    private static class Attachment {
        ByteBuffer buffer;
        AsynchronousSocketChannel socket;
        Reader reader;
        ServeStatistic serveStatistic;
    }

    private class ReaderHandler implements CompletionHandler<Integer, Attachment> {

        @Override
        public void completed(Integer result, Attachment attachment) {
            Reader reader = attachment.reader;
            ByteBuffer buffer = attachment.buffer;
            AsynchronousSocketChannel socket = attachment.socket;
            reader.readFromBuffer(buffer);
            if (!reader.finished()) {
                socket.read(buffer, attachment, this);
            } else {
                MessageProtos.Message message = reader.parseMessage();
                List<Integer> array = new ArrayList<>(message.getArrayList());
                SortStatistic sortStatistic = new SortStatistic();
                sortStatistic.setStartTime();
                SortAlgorithm.sort(array);
                sortStatistic.setEndTime();
                sortStatistics.add(sortStatistic);
                MessageProtos.Message response = MessageProtos.Message.newBuilder().addAllArray(array).build();
                byte[] bytes = response.toByteArray();
                if (buffer.capacity() < bytes.length + 4) {
                    buffer = ByteBuffer.allocate(bytes.length + 4);
                    attachment.buffer = buffer;
                }
                buffer.putInt(bytes.length);
                buffer.put(bytes);
                buffer.flip();
                attachment.reader = new Reader();
                attachment.serveStatistic = reader.getServeStatistic();
                socket.write(buffer, attachment, new WriterHandler());
            }
        }

        @Override
        public void failed(Throwable exc, Attachment attachment) {
            exc.printStackTrace();
        }
    }

    private class WriterHandler implements CompletionHandler<Integer, Attachment> {
        @Override
        public void completed(Integer result, Attachment attachment) {
            AsynchronousSocketChannel socket = attachment.socket;
            ByteBuffer buffer = attachment.buffer;

            if (result > 0) {
                socket.write(buffer, attachment, this);
            } else {
                buffer.clear();
                attachment.serveStatistic.setEndTime();
                serveStatistics.add(attachment.serveStatistic);
                attachment.serveStatistic = null;
                socket.read(buffer, attachment, new ReaderHandler());
            }
        }

        @Override
        public void failed(Throwable exc, Attachment attachment) {
            exc.printStackTrace();
        }
    }

    // должен считать ровно один массив
    private class Reader {
        private byte[] data = null;
        private int offset = 0;
        private ServeStatistic serveStatistic = null;

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

        void readFromBuffer(ByteBuffer buffer) {
            buffer.flip();
            while (buffer.remaining() > 0) {
                if (data == null && buffer.remaining() < 4) {
                    break;
                }
                if (data == null) {
                    int byteSize = buffer.getInt();
                    serveStatistic = new ServeStatistic();
                    serveStatistic.setStartTime();
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

        public ServeStatistic getServeStatistic() {
            return serveStatistic;
        }
    }
}
