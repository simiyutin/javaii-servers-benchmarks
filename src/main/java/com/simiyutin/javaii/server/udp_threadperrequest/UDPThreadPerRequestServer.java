package com.simiyutin.javaii.server.udp_threadperrequest;

import com.google.protobuf.InvalidProtocolBufferException;
import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.proto.SerializationWrapper;
import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.SortAlgorithm;
import com.simiyutin.javaii.statistics.ServeStatistic;
import com.simiyutin.javaii.statistics.SortStatistic;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

public class UDPThreadPerRequestServer extends Server {
    private DatagramSocket serverSocket;
    private Thread listener;
    private final int port;
    private final byte[] buffer;

    public UDPThreadPerRequestServer(int port) {
        this.port = port;
        this.buffer = new byte[60000];
    }

    @Override
    public void start() throws IOException {
        this.serverSocket = new DatagramSocket(port);
        listener = new Thread(() -> {
            while (true) {
                try {
                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                    serverSocket.receive(receivePacket);
                    ServeStatistic serveStatistic = new ServeStatistic();
                    serveStatistic.setStartTime();
                    new Thread(() -> {
                        try {
                            MessageProtos.Message message = SerializationWrapper.deserialize(new ByteArrayInputStream(receivePacket.getData()));
                            List<Integer> array = new ArrayList<>(message.getArrayList());
                            SortStatistic sortStatistic = new SortStatistic();
                            sortStatistic.setStartTime();
                            SortAlgorithm.sort(array);
                            sortStatistic.setEndTime();
                            sortStatistics.add(sortStatistic);

                            MessageProtos.Message response = MessageProtos.Message.newBuilder().addAllArray(array).build();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                            SerializationWrapper.serialize(response, baos);

                            byte[] bytes = baos.toByteArray();
                            System.arraycopy(bytes, 0, buffer, 0, bytes.length);
                            DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length, receivePacket.getAddress(), receivePacket.getPort()); // todo ???
                            serverSocket.send(responsePacket);
                            serveStatistic.setEndTime();
                            serveStatistics.add(serveStatistic);
                        } catch (InvalidProtocolBufferException ex) {
                            System.out.println("server: invalid protobuf message");
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
