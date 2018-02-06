package com.simiyutin.javaii.server.udp_threadperrequest;

import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.SortAlgorithm;

import java.io.*;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;
import java.util.Arrays;

public class UDPThreadPerRequestServer extends Server {
    private final DatagramSocket serverSocket;

    public UDPThreadPerRequestServer(int port) throws SocketException {
        this.serverSocket = new DatagramSocket(port);
    }

    @Override
    public void start() throws IOException {
        while (true) {
            byte[] receive = new byte[1024];
            DatagramPacket receivePacket = new DatagramPacket(receive, receive.length); //todo ???
            serverSocket.receive(receivePacket);
            new Thread(() -> {
                try {
                    DataInputStream dis = new DataInputStream(new ByteArrayInputStream(receivePacket.getData()));
                    int n = dis.readInt();
                    int[] array = new int[n];
                    for (int i = 0; i < n; i++) {
                        array[i] = dis.readInt();
                    }
                    SortAlgorithm.sort(array);

                    ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
                    DataOutputStream dos = new DataOutputStream(baos);
                    dos.writeInt(n);
                    for (int val : array) {
                        dos.writeInt(val);
                    }

                    byte[] response = baos.toByteArray();

                    DatagramPacket responsePacket = new DatagramPacket(response, response.length, receivePacket.getAddress(), receivePacket.getPort()); // todo ???
                    serverSocket.send(responsePacket);

                } catch (IOException ex) {
                    ex.printStackTrace();
                }
            }).start();
        }
    }
}
