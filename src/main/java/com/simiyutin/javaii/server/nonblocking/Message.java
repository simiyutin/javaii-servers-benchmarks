package com.simiyutin.javaii.server.nonblocking;

import java.nio.channels.SocketChannel;
import java.util.List;

public class Message {
    List<Integer> data;
    SocketChannel channel;

    public Message(List<Integer> data, SocketChannel channel) {
        this.data = data;
        this.channel = channel;
    }
}