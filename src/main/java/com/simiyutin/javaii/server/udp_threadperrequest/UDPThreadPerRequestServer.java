package com.simiyutin.javaii.server.udp_threadperrequest;

import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.proto.SerializationWrapper;
import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.SortAlgorithm;
import com.simiyutin.javaii.statistics.ServerServeTimeStatistic;
import com.simiyutin.javaii.statistics.ServerSortTimeStatistic;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class UDPThreadPerRequestServer extends Server {
    private DatagramSocket serverSocket;
    private Thread listener;
    private final int port;

    public UDPThreadPerRequestServer(int port) {
        this.port = port;
    }

    @Override
    public void start() throws IOException {
        this.serverSocket = new DatagramSocket(port);
        listener = new Thread(() -> {
            while (true) {
                try {

                    byte[] receive = new byte[1024];
                    DatagramPacket receivePacket = new DatagramPacket(receive, receive.length); //todo ???
                    serverSocket.receive(receivePacket);
                    new Thread(() -> {
                        try {
                            MessageProtos.Message message = SerializationWrapper.deserialize(new ByteArrayInputStream(receivePacket.getData()));
                            List<Integer> array = new ArrayList<>(message.getArrayList());
                            long sortTime = SortAlgorithm.sort(array);
                            sortTimeStatistics.add(new ServerSortTimeStatistic(sortTime));
                            serveTimeStatistics.add(new ServerServeTimeStatistic(sortTime));

                            MessageProtos.Message response = MessageProtos.Message.newBuilder().addAllArray(array).build();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                            SerializationWrapper.serialize(response, baos);

                            byte[] bytes = baos.toByteArray();
                            DatagramPacket responsePacket = new DatagramPacket(bytes, bytes.length, receivePacket.getAddress(), receivePacket.getPort()); // todo ???
                            serverSocket.send(responsePacket);

                        } catch (IOException ex) {
                            ex.printStackTrace();
                        }
                    }).start();
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
        serverSocket.close();
    }
}
