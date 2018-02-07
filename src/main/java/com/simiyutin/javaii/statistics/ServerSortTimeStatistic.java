package com.simiyutin.javaii.statistics;

public class ServerSortTimeStatistic {
    public long timeMillis;

    public ServerSortTimeStatistic(long timeMillis) {
        this.timeMillis = timeMillis;
    }

    @Override
    public String toString() {
        return "ServerSortTimeStatistic{" +
                "timeMillis=" + timeMillis +
                '}';
    }
}
