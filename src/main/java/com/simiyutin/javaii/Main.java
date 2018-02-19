package com.simiyutin.javaii;

import com.simiyutin.javaii.client.Client;
import com.simiyutin.javaii.client.TCPStatefulClient;
import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.tcp_threadperclient.TCPThreadPerClientServer;
import com.simiyutin.javaii.statistics.ClientWorkTimeStatistic;
import com.simiyutin.javaii.statistics.StatisticsProcessor;
import com.simiyutin.javaii.testarch.ClientRunner;
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
        conf.clientArraySize = 1000;
        conf.clientDeltaMillis = 100;
        conf.clientNumberOfRequests = 10;
        conf.numberOfClients = 10;

        String host = "localhost";
        int port = 11111;

        Supplier<Server> serverSupplier = () -> {
            try {
                return new TCPThreadPerClientServer(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        };

        Supplier<Client> clientSupplier = () -> new TCPStatefulClient(host, port, conf);

        runTest(serverSupplier, clientSupplier, conf, statisticsProcessor);
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
