package com.simiyutin.javaii.statistics;

import com.simiyutin.javaii.testarch.Configuration;

import java.util.List;

public class StatisticsProcessor {
    public void process(List<ClientWorkTimeStatistic> clientWorkTimeStatistics,
                        List<ServerSortTimeStatistic> serverSortTimeStatistics,
                        List<ServerServeTimeStatistic> serverServeTimeStatistics,
                        Configuration conf) {
        System.out.println(clientWorkTimeStatistics);
        System.out.println(serverSortTimeStatistics);
        System.out.println(serverServeTimeStatistics);
    }
}
