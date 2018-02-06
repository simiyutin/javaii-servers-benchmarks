package com.simiyutin.javaii.server.tcp_nonblocking;

import com.simiyutin.javaii.proto.MessageProtos;

import java.nio.channels.SocketChannel;

public class Result {
    MessageProtos.Message message;
    SocketChannel channel;

    public Result(MessageProtos.Message message, SocketChannel channel) {
        this.message = message;
        this.channel = channel;
    }
}
