package com.simiyutin.javaii.server.udp_threadpool;

import com.google.protobuf.InvalidProtocolBufferException;
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

public class UDPThreadpoolServer extends Server {
    private DatagramSocket serverSocket;
    private final Queue<Response> writeQueue;
    private final ExecutorService threadPool;
    private Thread writer;
    private Thread listener;
    private final int port;
    private final byte[] buffer;

    public UDPThreadpoolServer(int port) {
        this.port = port;
        this.writeQueue = new ArrayBlockingQueue<>(1024);
        this.threadPool = Executors.newCachedThreadPool();
        this.buffer = new byte[60000];
    }

    @Override
    public void start() throws IOException {
        this.serverSocket = new DatagramSocket(port);
        writer = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    Response response = writeQueue.poll();
                    while (response != null) {
                        long endTime = System.currentTimeMillis();
                        serveTimeStatistics.add(new ServerServeTimeStatistic(endTime - response.startTime));

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

                } catch (InvalidProtocolBufferException ex) {
                    System.out.println("server: invalid protobuf message");
                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        writer.start();

        listener = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                    serverSocket.receive(receivePacket);
                    System.out.println("received packet!");
                    MessageProtos.Message message = SerializationWrapper.deserialize(new ByteArrayInputStream(receivePacket.getData()));
                    List<Integer> array = new ArrayList<>(message.getArrayList());

                    long startTime = System.currentTimeMillis();
                    threadPool.submit(() -> {
                        long sortTime = SortAlgorithm.sort(array);
                        sortTimeStatistics.add(new ServerSortTimeStatistic(sortTime));
                        Response response = new Response();
                        response.array = array;
                        response.address = receivePacket.getAddress();
                        response.port = receivePacket.getPort();
                        response.startTime = startTime;
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

    private static class Response {
        List<Integer> array;
        InetAddress address;
        int port;
        long startTime;
    }
}
