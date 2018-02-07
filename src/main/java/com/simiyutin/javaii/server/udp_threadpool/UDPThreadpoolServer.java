package com.simiyutin.javaii.server.udp_threadpool;

import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.proto.SerializationWrapper;
import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.SortAlgorithm;
import com.simiyutin.javaii.statistics.ServerServeTimeStatistic;
import com.simiyutin.javaii.statistics.ServerSortTimeStatistic;

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

public class UDPThreadpoolServer implements Server {
    private final DatagramSocket serverSocket;
    private final Queue<Response> writeQueue;
    private final ExecutorService threadPool;
    private Thread writer;
    private Thread listener;

    public UDPThreadpoolServer(int port) throws SocketException {
        this.serverSocket = new DatagramSocket(port);
        this.writeQueue = new ArrayBlockingQueue<>(1024);
        this.threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void start() throws IOException {

        writer = new Thread(() -> {
            while (!Thread.interrupted()) {
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
                        return;
                    }

                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        writer.start();

        listener = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
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
                catch (SocketException ex) {
                    return;
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        listener.start();
    }

    @Override
    public void stop() {
        if (listener != null) {
            listener.interrupt();
        }

        if (writer != null) {
            writer.interrupt();
        }

        serverSocket.close();
        threadPool.shutdown();
    }

    @Override
    public List<ServerSortTimeStatistic> getSortTimeStatistics() {
        return null;
    }

    @Override
    public List<ServerServeTimeStatistic> getServeTimeStatistics() {
        return null;
    }

    private static class Response {
        List<Integer> array;
        InetAddress address;
        int port;
    }
}
