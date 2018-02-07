package com.simiyutin.javaii.statistics;

public class ClientWorkTimeStatistic {
    public long timeMillis;

    public ClientWorkTimeStatistic(long timeMillis) {
        this.timeMillis = timeMillis;
    }

    @Override
    public String toString() {
        return "ClientWorkTimeStatistic{" +
                "timeMillis=" + timeMillis +
                '}';
    }
}
