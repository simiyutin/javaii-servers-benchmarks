package com.simiyutin.javaii.server;

import com.simiyutin.javaii.statistics.ServerServeTimeStatistic;
import com.simiyutin.javaii.statistics.ServerSortTimeStatistic;

import java.io.IOException;
import java.util.List;

public interface Server {
    void start() throws IOException;
    void stop();
    List<ServerSortTimeStatistic> getSortTimeStatistics();
    List<ServerServeTimeStatistic> getServeTimeStatistics();
}
