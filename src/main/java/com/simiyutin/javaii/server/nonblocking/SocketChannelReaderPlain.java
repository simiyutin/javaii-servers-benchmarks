package com.simiyutin.javaii.server.nonblocking;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class SocketChannelReaderPlain {
    private List<List<Integer>> fullMessages = new ArrayList<>();
    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    private List<Integer> constructedMessage = new ArrayList<>();
    private int curSize = -1;

    void read(SocketChannel channel) throws IOException {
        while (channel.read(buffer) > 0) {
            buffer.flip();

            while (buffer.remaining() >= 4) {
                int readValue = buffer.getInt();
                if (curSize == -1) {
                    curSize = readValue;
                } else if (constructedMessage.size() == curSize) {
                    fullMessages.add(constructedMessage);
                    curSize = readValue;
                    constructedMessage = new ArrayList<>();
                } else {
                    constructedMessage.add(readValue);
                }
            }

            if (constructedMessage.size() == curSize) {
                fullMessages.add(constructedMessage);
                curSize = -1;
                constructedMessage = new ArrayList<>();
            }

            buffer.compact();
        }
    }

    List<List<Integer>> getFullMessages() {
        List<List<Integer>> result = fullMessages;
        fullMessages = new ArrayList<>();
        return result;
    }
}
