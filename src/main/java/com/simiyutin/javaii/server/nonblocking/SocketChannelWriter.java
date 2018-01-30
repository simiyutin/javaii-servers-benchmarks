package com.simiyutin.javaii.server.nonblocking;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class SocketChannelWriter {
    private Queue<List<Integer>> messageQueue = new ArrayBlockingQueue<>(1024);
    private ByteBuffer curBuffer = ByteBuffer.allocate(128);

    public void addData(List<Integer> data) {
        messageQueue.add(data);
        curBuffer.flip();
    }

    public void write(SocketChannel channel, Runnable unsubscribeHook) throws IOException {

        int bytesWritten;
        do {
            // contract - curBuffer is always in read-mode outside this if block
            if (!curBuffer.hasRemaining()) {
                List<Integer> data = messageQueue.poll();
                if (curBuffer.capacity() < (data.size() + 1) * 4) {
                    curBuffer = ByteBuffer.allocate((data.size() + 1) * 4);
                }
                curBuffer.clear(); // now in write-mode
                curBuffer.putInt(data.size());
                for (Integer value : data) {
                    curBuffer.putInt(value);
                }
                curBuffer.flip();
            }
            bytesWritten = channel.write(curBuffer);

        } while (bytesWritten > 0 && !messageQueue.isEmpty());

        if (messageQueue.isEmpty()) {
            unsubscribeHook.run();
        }
    }
}
