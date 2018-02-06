package com.simiyutin.javaii.client;

import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.tcp_async.TCPAsyncServer;
import com.simiyutin.javaii.server.tcp_nonblocking.TCPNonBlockingServer;
import com.simiyutin.javaii.server.tcp_threadperclient.TCPThreadPerClientServer;
import com.simiyutin.javaii.server.tcp_threadpool.TCPThreadPoolServer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

public class Main {

    // количесвто одновременно работающих лиентов
    private static final int M = 10;

    public static void main(String[] args) throws IOException {

        String host = "localhost";
        int port = 11111;

        Supplier<Server> serverSupplier = () -> {
            try {
                return new TCPAsyncServer(port);
            } catch (IOException e) {
                e.printStackTrace();
            }
            return null;
        };

        Supplier<Client> clientSupplier = () -> new TCPStatefulClient(host, port);

        runTest(serverSupplier, clientSupplier);
    }

    private static void runTest(Supplier<Server> serverSupplier, Supplier<Client> clientSupplier) {
        new Thread(() -> {
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
        }).start();

        try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        CyclicBarrier barrier = new CyclicBarrier(M);

        List<Thread> clients = new ArrayList<>();
        for (int i = 0; i < M; i++) {
            clients.add(new Thread(() -> {
                try {
                    barrier.await();
                } catch (InterruptedException | BrokenBarrierException e) {
                    e.printStackTrace();
                }
                Client client = clientSupplier.get();
                try {
                    client.start();
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
        System.exit(0); // todo make threads deamons
    }
}
