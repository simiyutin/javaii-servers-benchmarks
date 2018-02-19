package com.simiyutin.javaii.testarch;

import com.simiyutin.javaii.client.Client;
import com.simiyutin.javaii.statistics.ClientWorkTimeStatistic;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.function.Supplier;

public class ClientRunner implements Runnable {
    private final CyclicBarrier barrier;
    private final Supplier<Client> clientSupplier;
    private final List<ClientWorkTimeStatistic> clientWorkTimeStatistics;

    public ClientRunner(CyclicBarrier barrier, Supplier<Client> clientSupplier, List<ClientWorkTimeStatistic> clientWorkTimeStatistics) {
        this.barrier = barrier;
        this.clientSupplier = clientSupplier;
        this.clientWorkTimeStatistics = clientWorkTimeStatistics;
    }

    @Override
    public void run() {
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
    }
}
