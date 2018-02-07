package com.simiyutin.javaii.server;

import com.simiyutin.javaii.statistics.ServerServeTimeStatistic;
import com.simiyutin.javaii.statistics.ServerSortTimeStatistic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Server {
    protected final List<ServerSortTimeStatistic> sortTimeStatistics = Collections.synchronizedList(new ArrayList<>());
    protected final List<ServerServeTimeStatistic> serveTimeStatistics = Collections.synchronizedList(new ArrayList<>());

    public abstract void start() throws IOException;
    public abstract void stop();
    public List<ServerSortTimeStatistic> getSortTimeStatistics() {
        return sortTimeStatistics;
    };
    public List<ServerServeTimeStatistic> getServeTimeStatistics() {
        return serveTimeStatistics;
    };
}
