package com.simiyutin.javaii.client;

import com.simiyutin.javaii.Configuration;

import java.io.IOException;

public abstract class Client {
    protected String host;
    protected int port;
    protected Configuration conf;

    public Client(String host, int port, Configuration conf) {
        this.host = host;
        this.port = port;
        this.conf = conf;
    }

    public abstract void start() throws IOException;
}
