package com.simiyutin.javaii.client;

import com.simiyutin.javaii.Configuration;
import com.simiyutin.javaii.statistics.ClientStatistics;

import java.io.IOException;

public abstract class Client {
    protected String host;
    protected int port;
    protected Configuration conf;
    private ClientStatistics statistics;

    public Client(String host, int port, Configuration conf) {
        this.host = host;
        this.port = port;
        this.conf = conf;
    }

    public void start() throws IOException {
        long start = System.currentTimeMillis();
        startImpl();
        long end = System.currentTimeMillis();
        statistics = new ClientStatistics();
        statistics.timeOfWorkMillis = end - start;
    }

    public ClientStatistics getStatistics() {
        return statistics;
    }

    public abstract void startImpl() throws IOException;
}
