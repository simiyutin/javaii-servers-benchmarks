package com.simiyutin.javaii.server.udp_threadpool;

import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.SortAlgorithm;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class UDPThreadpoolServer extends Server {
    private final DatagramSocket serverSocket;
    private final Queue<Response> writeQueue;
    private final ExecutorService threadPool;

    public UDPThreadpoolServer(int port) throws SocketException {
        this.serverSocket = new DatagramSocket(port);
        this.writeQueue = new ArrayBlockingQueue<>(1024);
        this.threadPool = Executors.newCachedThreadPool();
    }

    private static class Response {
        int[] array;
        InetAddress address;
        int port;
    }

    @Override
    public void start() throws IOException {
        new Thread(() -> { // todo move to separate class
            while (true) {
                try {
                    Response response = writeQueue.poll();
                    while (response != null) {
                        int[] array = response.array;
                        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                        DataOutputStream dos = new DataOutputStream(baos);
                        dos.writeInt(array.length);
                        for (int val : array) {
                            dos.writeInt(val);
                        }

                        byte[] data = baos.toByteArray();
                        DatagramPacket responsePacket = new DatagramPacket(data, data.length, response.address, response.port); // todo ???
                        serverSocket.send(responsePacket);
                        response = writeQueue.poll();
                    }

                    try {
                        TimeUnit.MILLISECONDS.sleep(100);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                }catch (IOException ex) {
                    ex.printStackTrace();
                }

            }
        }).start();

        while (true) {
            byte[] receive = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receive, receive.length); //todo ???
            serverSocket.receive(receivePacket);
            DataInputStream dis = new DataInputStream(new ByteArrayInputStream(receivePacket.getData()));
            int n = dis.readInt();
            int[] array = new int[n];
            for (int i = 0; i < n; i++) {
                array[i] = dis.readInt();
            }

            threadPool.submit(() -> {
                SortAlgorithm.sort(array);
                Response response = new Response();
                response.array = array;
                response.address = receivePacket.getAddress();
                response.port = receivePacket.getPort();
                writeQueue.add(response);
            });
        }
    }
}
