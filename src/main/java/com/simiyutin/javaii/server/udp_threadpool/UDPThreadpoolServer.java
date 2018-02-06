package com.simiyutin.javaii.server.udp_threadpool;

import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.proto.SerializationWrapper;
import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.SortAlgorithm;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
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
        List<Integer> array;
        InetAddress address;
        int port;
    }

    @Override
    public void start() throws IOException {
        new Thread(() -> { // todo move to separate writer class
            while (true) {
                try {
                    Response response = writeQueue.poll();
                    while (response != null) {
                        List<Integer> array = response.array;
                        MessageProtos.Message message = MessageProtos.Message.newBuilder().addAllArray(array).build();
                        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                        SerializationWrapper.serialize(message, baos);

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
            MessageProtos.Message message = SerializationWrapper.deserialize(new ByteArrayInputStream(receivePacket.getData()));
            List<Integer> array = new ArrayList<>(message.getArrayList());

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
