package com.simiyutin.javaii.client;

import com.simiyutin.javaii.client.communicators.UDPSerialCommunicator;
import com.simiyutin.javaii.testarch.Configuration;

import java.io.IOException;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class UDPTrivialClient extends Client {
    private int id;
    private final byte[] buffer;

    public UDPTrivialClient(Configuration conf) {
        super(conf);
        this.buffer = new byte[60000];
    }


    @Override
    public void startImpl() throws IOException {
        DatagramSocket socket = new DatagramSocket();
        socket.setSoTimeout(1000);
        InetAddress address = InetAddress.getByName(conf.host);
        for (int i = 0; i < conf.clientNumberOfRequests;) {
            DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, address, conf.port);
            socket.send(sendPacket);
            System.out.println(String.format("client #%d, packet %d: sent!", id, i));
            DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
            try {
                socket.receive(receivePacket);
            } catch (SocketTimeoutException ex) {
                System.out.println("got timeout, trying again");
                continue;
            }
            i++;
            System.out.println(String.format("client #%d, packet %d: received!", id, i));
        }
    }
}
