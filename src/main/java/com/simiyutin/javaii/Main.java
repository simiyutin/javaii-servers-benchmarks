package com.simiyutin.javaii;

import com.simiyutin.javaii.client.Client;
import com.simiyutin.javaii.client.UDPClient;
import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.udp_threadpool.UDPThreadpoolServer;
import com.simiyutin.javaii.statistics.ClientWorkTimeStatistic;
import com.simiyutin.javaii.statistics.StatisticsProcessor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.function.Supplier;

public class Main {
    private static final List<ClientWorkTimeStatistic> clientWorkTimeStatistics = new ArrayList<>();

    // Для каждого сервера запускаем тест. Считаем зависимость первых двух метрик от времени и среднеее значения третьей метрики по всем M клиентам
    public static void main(String[] args) throws IOException {
        StatisticsProcessor statisticsProcessor = new StatisticsProcessor();

        Configuration conf = new Configuration();
        conf.clientArraySize = 10;
        conf.clientDeltaMillis = 100;
        conf.clientNumberOfRequests = 10;
        conf.numberOfClients = 10;

        String host = "localhost";
        int port = 11111;

        Supplier<Server> serverSupplier = () -> {
            try {
                return new UDPThreadpoolServer(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        };

        Supplier<Client> clientSupplier = () -> new UDPClient(host, port, conf);

        runTest(serverSupplier, clientSupplier, conf, statisticsProcessor);
    }

    private static void runTest(Supplier<Server> serverSupplier, Supplier<Client> clientSupplier, Configuration conf, StatisticsProcessor statisticsProcessor) {
        Server server = serverSupplier.get();
        if (server != null) {
            try {
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            throw new AssertionError("failed to init server");
        }


        CyclicBarrier barrier = new CyclicBarrier(conf.numberOfClients);

        List<Thread> clients = new ArrayList<>();
        for (int i = 0; i < conf.numberOfClients; i++) {
            clients.add(new Thread(() -> {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                Client client = clientSupplier.get();
                try {
                    client.start();
                    clientWorkTimeStatistics.add(client.getStatistic());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }));
            clients.get(i).start();
        }

        for (Thread thread : clients) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        System.out.println("all right!");
        server.stop();

        statisticsProcessor.process(
                clientWorkTimeStatistics,
                server.getSortTimeStatistics(),
                server.getServeTimeStatistics(),
                conf);
    }
}
