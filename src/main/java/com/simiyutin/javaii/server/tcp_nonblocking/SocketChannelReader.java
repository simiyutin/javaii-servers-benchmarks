package com.simiyutin.javaii.server.tcp_nonblocking;

import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.statistics.ServeStatistic;
import com.sun.xml.internal.ws.api.message.MessageWritable;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;

public class SocketChannelReader {
    private List<MessageStatistic> fullMessages = new ArrayList<>();
    private ByteBuffer buffer = ByteBuffer.allocate(1024);
    private byte[] data = null;
    private int offset = 0;
    private ServeStatistic serveStatistic;


    void read(SocketChannel channel) throws IOException {
        while (channel.read(buffer) > 0) {
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
                    MessageProtos.Message message = MessageProtos.Message.parseFrom(data);
                    MessageStatistic wrapper = new MessageStatistic();
                    wrapper.message = message;
                    wrapper.serveStatistic = serveStatistic;
                    serveStatistic = null;
                    fullMessages.add(wrapper);
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
                MessageStatistic wrapper = new MessageStatistic();
                wrapper.message = message;
                wrapper.serveStatistic = serveStatistic;
                serveStatistic = null;
                fullMessages.add(wrapper);
                data = null;
                offset = 0;
            }

            buffer.compact();
        }
    }

    List<MessageStatistic> getFullMessages() {
        List<MessageStatistic> result = fullMessages;
        fullMessages = new ArrayList<>();
        return result;
    }
}
