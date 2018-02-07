package com.simiyutin.javaii.client;

import com.simiyutin.javaii.Configuration;
import com.simiyutin.javaii.statistics.ClientWorkTimeStatistic;

import java.io.IOException;

public abstract class Client {
    protected String host;
    protected int port;
    protected Configuration conf;
    private ClientWorkTimeStatistic statistic;

    public Client(String host, int port, Configuration conf) {
        this.host = host;
        this.port = port;
        this.conf = conf;
    }

    public void start() throws IOException {
        long start = System.currentTimeMillis();
        startImpl();
        long end = System.currentTimeMillis();
        statistic = new ClientWorkTimeStatistic(end - start);
    }

    public ClientWorkTimeStatistic getStatistic() {
        return statistic;
    }

    public abstract void startImpl() throws IOException;
}
