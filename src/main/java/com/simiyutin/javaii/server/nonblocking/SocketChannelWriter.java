package com.simiyutin.javaii.server.nonblocking;

import com.google.protobuf.CodedOutputStream;
import com.simiyutin.javaii.proto.MessageProtos;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class SocketChannelWriter {
    private Queue<byte[]> messageQueue = new ArrayBlockingQueue<>(1024);
    private ByteBuffer curBuffer = ByteBuffer.allocate(128);

    public void addMessage(MessageProtos.Message message) {
        messageQueue.add(message.toByteArray());
        curBuffer.flip();
    }

    public void write(SocketChannel channel, Runnable unsubscribeHook) throws IOException {

        int bytesWritten;
        do {
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
            bytesWritten = channel.write(curBuffer);
        } while (bytesWritten > 0 && !messageQueue.isEmpty());

        if (messageQueue.isEmpty()) {
            unsubscribeHook.run();
        }
    }
}
