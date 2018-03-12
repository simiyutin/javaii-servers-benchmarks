package com.simiyutin.javaii;

import com.simiyutin.javaii.client.Client;
import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.statistics.ClientStatistic;
import com.simiyutin.javaii.statistics.StatisticsProcessor;
import com.simiyutin.javaii.testarch.ClientServerFactory;
import com.simiyutin.javaii.testarch.ClientRunner;
import com.simiyutin.javaii.testarch.ClientServer;
import com.simiyutin.javaii.testarch.Configuration;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class Main {
    private static final List<ClientStatistic> clientWorkTimeStatistics = Collections.synchronizedList(new ArrayList<>());

    public static void main(String[] args) {

        String selectedArch = args[0];
        int numberOfRequests = Integer.parseInt(args[1]);
        String varying = args[2];
        int varMin = Integer.parseInt(args[3]);
        int varMax = Integer.parseInt(args[4]);
        int varStep = Integer.parseInt(args[5]);
        String secondParam = args[6];
        int secondVal = Integer.parseInt(args[7]);
        String thirdParam = args[8];
        int thirdVal = Integer.parseInt(args[9]);

        runTestSeries(
                selectedArch,
                numberOfRequests,
                varying,
                varMin,
                varMax,
                varStep,
                secondParam,
                secondVal,
                thirdParam,
                thirdVal
        );
    }

    private static void allArchsRun() {
        int numberOfRequests = 20;
        String varying = "N";
        int varMin = 1000;
        int varMax = 5000;
        int varStep = 1000;
        String secondParam = "M";
        int secondVal = 10;
        String thirdParam = "D";
        int thirdVal = 10;

        List<String> archs = Arrays.asList(
                "tcp_serial",
                "tcp_threadperclient",
                "tcp_threadpool",
                "tcp_nonblocking",
                "tcp_async",
                "udp_threadpool",
                "udp_threadperrequest"
        );

        for (String selectedArch : archs) {
            runTestSeries(
                    selectedArch,
                    numberOfRequests,
                    varying,
                    varMin,
                    varMax,
                    varStep,
                    secondParam,
                    secondVal,
                    thirdParam,
                    thirdVal
            );
        }
    }

    private static void runTestSeries(
            String selectedArch,
            int X,
            String varying,
            int varMin,
            int varMax,
            int varStep,
            String secondParam,
            int secondVal,
            String thirdParam,
            int thirdVal

    ) {
        Map<String, Integer> params = new HashMap<>();
        params.put(varying, varMin);
        params.put(secondParam, secondVal);
        params.put(thirdParam, thirdVal);

        while (params.get(varying) <= varMax) {
            StatisticsProcessor statisticsProcessor = new StatisticsProcessor();

            Configuration conf = new Configuration();
            conf.clientArraySize = params.get("N");
            conf.clientDeltaMillis = params.get("D");
            conf.clientNumberOfRequests = X;
            conf.numberOfClients = params.get("M");
            conf.host = "localhost";
            conf.port = 11111;

            System.out.println(String.format(
                    "array size: %d, delta: %d, number of requests: %d, number of clients: %d, host: %s, port: %d",
                    conf.clientArraySize, conf.clientDeltaMillis, conf.clientNumberOfRequests, conf.numberOfClients, conf.host, conf.port
                    ));

            ClientServer clientServer = ClientServerFactory.getConfiguration(selectedArch, conf);
            runTestWithConfig(clientServer.getServerSupplier(), clientServer.getClientSupplier(), conf, selectedArch, varying, statisticsProcessor);
            try {
                System.out.println("waiting for port freeing..");
                TimeUnit.SECONDS.sleep(2);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            params.put(varying, params.get(varying) + varStep);
        }
    }

    private static void runTestWithConfig(Supplier<Server> serverSupplier, Supplier<Client> clientSupplier, Configuration conf, String curArch, String varyingOpt, StatisticsProcessor statisticsProcessor) {
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
                System.out.println("client joined!");
            } catch (InterruptedException e) {
                throw new AssertionError("failed to join client");
            }
        }

        System.out.println("all clients finished!");
        server.stop();

        System.out.println("processing results..");
        statisticsProcessor.process(
                clientWorkTimeStatistics,
                server.getSortStatistics(),
                server.getServeStatistics(),
                conf, curArch, varyingOpt);
    }
}
