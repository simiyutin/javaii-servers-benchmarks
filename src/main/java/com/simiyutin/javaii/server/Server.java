package com.simiyutin.javaii.server;

import com.simiyutin.javaii.statistics.ServeStatistic;
import com.simiyutin.javaii.statistics.SortStatistic;
import com.simiyutin.javaii.statistics.Statistic;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public abstract class Server {
    protected final List<SortStatistic> sortStatistics = Collections.synchronizedList(new ArrayList<>());
    protected final List<ServeStatistic> serveStatistics = Collections.synchronizedList(new ArrayList<>());

    public abstract void start() throws IOException;
    public abstract void stop();
    public List<SortStatistic> getSortStatistics() {
        return sortStatistics;
    };
    public List<ServeStatistic> getServeStatistics() {
        return serveStatistics;
    };
}
