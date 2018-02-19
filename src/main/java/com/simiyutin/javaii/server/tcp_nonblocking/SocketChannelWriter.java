package com.simiyutin.javaii.server.tcp_nonblocking;

import com.simiyutin.javaii.proto.MessageProtos;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class SocketChannelWriter {
    private Queue<byte[]> messageQueue = new ArrayBlockingQueue<>(1024); // todo может быть обычная очередь
    private ByteBuffer curBuffer = ByteBuffer.allocate(128);

    public SocketChannelWriter() {
        curBuffer.flip();
    }

    public void addMessage(MessageProtos.Message message) {
        messageQueue.add(message.toByteArray());
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
        }

        if (messageQueue.isEmpty() && !curBuffer.hasRemaining()) {
            unsubscribeHook.run();
        }
    }
}
