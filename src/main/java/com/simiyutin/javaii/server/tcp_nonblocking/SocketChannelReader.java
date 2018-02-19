package com.simiyutin.javaii.server.tcp_nonblocking;

import com.simiyutin.javaii.proto.MessageProtos;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class SocketChannelReader {
    private List<MessageProtos.Message> fullMessages = new ArrayList<>();
    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    private byte[] data = null;
    private int offset = 0;


    void read(SocketChannel channel) throws IOException {
        while (channel.read(buffer) > 0) {
            buffer.flip();

            while (buffer.remaining() > 0) {
                if (data == null && buffer.remaining() < 4) {
                    break;
                }

                if (data == null) {
                    int byteSize = buffer.getInt();
                    data = new byte[byteSize];
                } else if (offset == data.length) {
                    MessageProtos.Message message = MessageProtos.Message.parseFrom(data);
                    fullMessages.add(message);
                    data = null;
                    offset = 0;
                } else {
                    int remaining = buffer.remaining();
                    buffer.get(data, offset, remaining);
                    offset += remaining;
                }
            }

            if (data != null && data.length == offset) {
                MessageProtos.Message message = MessageProtos.Message.parseFrom(data);
                fullMessages.add(message);
                data = null;
                offset = 0;
            }

            buffer.compact();
        }
    }

    List<MessageProtos.Message> getFullMessages() {
        List<MessageProtos.Message> result = fullMessages;
        fullMessages = new ArrayList<>();
        return result;
    }
}
