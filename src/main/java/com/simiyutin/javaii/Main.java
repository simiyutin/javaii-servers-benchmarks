package com.simiyutin.javaii;

import com.simiyutin.javaii.client.Client;
import com.simiyutin.javaii.client.TCPStatefulClient;
import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.tcp_threadperclient.TCPThreadPerClientServer;
import com.simiyutin.javaii.statistics.ClientWorkTimeStatistic;
import com.simiyutin.javaii.statistics.StatisticsProcessor;
import com.simiyutin.javaii.testarch.ApplicationConfigurationFactory;
import com.simiyutin.javaii.testarch.ClientRunner;
import com.simiyutin.javaii.testarch.ClientServer;
import com.simiyutin.javaii.testarch.Configuration;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.function.Supplier;

public class Main {
    private static final List<ClientWorkTimeStatistic> clientWorkTimeStatistics = Collections.synchronizedList(new ArrayList<>());

    // Для каждого сервера запускаем тест. Считаем зависимость первых двух метрик от времени и среднеее значения третьей метрики по всем M клиентам
    public static void main(String[] args) throws IOException {
        StatisticsProcessor statisticsProcessor = new StatisticsProcessor();

        Configuration conf = new Configuration();
        conf.clientArraySize = 10;
        conf.clientDeltaMillis = 120;
        conf.clientNumberOfRequests = 100;
        conf.numberOfClients = 10;
        conf.host = "localhost";
        conf.port = 11111;

        ClientServer clientServer = ApplicationConfigurationFactory.getConfiguration("udp_threadpool", conf);

        runTest(clientServer.getServerSupplier(), clientServer.getClientSupplier(), conf, statisticsProcessor);
    }

    private static void runTest(Supplier<Server> serverSupplier, Supplier<Client> clientSupplier, Configuration conf, StatisticsProcessor statisticsProcessor) {
        System.out.println("starting server..");
        Server server = serverSupplier.get();
        if (server != null) {
            try {
                server.start();
            } catch (IOException e) {
                throw new AssertionError("failed to start server");
            }
        } else {
            throw new AssertionError("failed to init server");
        }

        System.out.println("initializing clients..");
        CyclicBarrier barrier = new CyclicBarrier(conf.numberOfClients);
        List<Thread> clients = new ArrayList<>();
        for (int i = 0; i < conf.numberOfClients; i++) {
            clients.add(new Thread(new ClientRunner(barrier, clientSupplier, clientWorkTimeStatistics)));
            clients.get(i).start();
        }

        System.out.println("waiting for clients..");
        for (Thread thread : clients) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new AssertionError("failed to join client");
            }
        }

        System.out.println("all clients finished!");
        server.stop();

        System.out.println("processing results..");
        statisticsProcessor.process(
                clientWorkTimeStatistics,
                server.getSortTimeStatistics(),
                server.getServeTimeStatistics(),
                conf);
    }
}
