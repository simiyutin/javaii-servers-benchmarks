package com.simiyutin.javaii.statistics;

// время, прошедшее между моментом, когда сообщение прочитано, и когда оно начинает отправляться
public class ServerServeTimeStatistic {
    public long timeMillis;

    public ServerServeTimeStatistic(long timeMillis) {
        this.timeMillis = timeMillis;
    }
}
