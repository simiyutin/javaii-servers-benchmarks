package com.simiyutin.javaii.server.tcp_nonblocking;

import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.statistics.ServeStatistic;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class SocketChannelWriter {
    private Queue<byte[]> messageQueue = new ArrayBlockingQueue<>(1024); // todo может быть обычная очередь
    private ByteBuffer curBuffer = ByteBuffer.allocate(128);
    private ServeStatistic serveStatistic;

    public SocketChannelWriter() {
        curBuffer.flip();
    }

    public void addMessage(MessageStatistic message) {
        messageQueue.add(message.message.toByteArray());
        if (messageQueue.size() > 1) {
            throw new AssertionError("assumed contract violated");
        }
        serveStatistic = message.serveStatistic;
    }

    public void write(SocketChannel channel, Runnable unsubscribeHook) throws IOException {

        while (!messageQueue.isEmpty()) {
            // contract - curBuffer is always in read-mode outside this if block
            if (!curBuffer.hasRemaining()) {
                byte[] message = messageQueue.poll();
                if (curBuffer.capacity() < message.length + 4) {
                    curBuffer = ByteBuffer.allocate(message.length + 4);
                }
                curBuffer.clear(); // now in write-mode
                curBuffer.putInt(message.length);

                curBuffer.put(message);
                curBuffer.flip();
            }
            int bytesWritten = channel.write(curBuffer);
            if (bytesWritten <= 0) {
                break;
            }
            if (!curBuffer.hasRemaining()) {
                if (serveStatistic == null) {
                    throw new AssertionError("assumed contract violated");
                }
                serveStatistic.setEndTime();
                serveStatistic = null;
            }
        }

        if (messageQueue.isEmpty() && !curBuffer.hasRemaining()) {
            unsubscribeHook.run();
        }
    }
}
