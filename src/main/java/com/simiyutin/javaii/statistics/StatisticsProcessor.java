package com.simiyutin.javaii.statistics;

import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.testarch.Configuration;

import java.io.*;
import java.util.List;

public class StatisticsProcessor {
    public void process(List<ClientWorkTimeStatistic> clientWorkTimeStatistics,
                        List<SortStatistic> serverSortTimeStatistics,
                        List<ServeStatistic> serverServeTimeStatistics,
                        Configuration conf,
                        String serverName, String varyingName) {

        String fileName = String.format(
                "data/%s_%s_%d_%d_%d_%d",
                serverName,
                varyingName,
                conf.clientArraySize,
                conf.clientDeltaMillis,
                conf.clientNumberOfRequests,
                conf.numberOfClients);

        try (BufferedWriter out = new BufferedWriter(new FileWriter(fileName))) {
            for (ClientWorkTimeStatistic s : clientWorkTimeStatistics) {
                out.write(s.timeMillis + ", ");
            }
            out.write("\n");
            for (ServeStatistic s : serverServeTimeStatistics) {
                out.write(s.getDiffTime() + ", ");
            }
            out.write("\n");
            for (SortStatistic s : serverSortTimeStatistics) {
                out.write(s.getDiffTime() + ", ");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
