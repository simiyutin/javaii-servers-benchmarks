package com.simiyutin.javaii.statistics;

public class Statistic {
    protected long startTime;
    protected long endTime;

    public void setStartTime() {
        this.startTime = System.currentTimeMillis();
    }

    public void setEndTime() {
        this.endTime = System.currentTimeMillis();
    }

    public long getDiffTime() {
        return endTime - startTime;
    }
}
