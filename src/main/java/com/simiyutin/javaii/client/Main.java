package com.simiyutin.javaii.client;

import com.simiyutin.javaii.server.SerialServer;
import com.simiyutin.javaii.server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BrokenBarrierException;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.TimeUnit;

public class Main {

    // количесвто одновременно работающих лиентов
    private static final int M = 10;

    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 11111;

        new Thread(() -> {
            try {
                Server server = new SerialServer(port);
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
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
                SerialClient client = new SerialClient();
                try {
                    client.start(host, port);
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

    }
}
