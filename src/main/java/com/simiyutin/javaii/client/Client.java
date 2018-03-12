package com.simiyutin.javaii.client;

import com.simiyutin.javaii.statistics.ClientStatistic;
import com.simiyutin.javaii.testarch.Configuration;

import java.io.IOException;

public abstract class Client {
    protected Configuration conf;
    private ClientStatistic statistic;

    public Client(Configuration conf) {
        this.conf = conf;
        this.statistic = new ClientStatistic();
    }

    public void start() throws IOException {
        statistic.setStartTime();
        startImpl();
        statistic.setEndTime();
    }

    public ClientStatistic getStatistic() {
        return statistic;
    }

    public abstract void startImpl() throws IOException;
}
