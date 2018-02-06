package com.simiyutin.javaii.client;

import com.simiyutin.javaii.client.communicators.UDPSerialCommunicator;
import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.proto.SerializationWrapper;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UDPClient extends Client {
    private static int gid = 0;
    private int id;

    public UDPClient(String host, int port) {
        super(host, port);
        this.id = gid++;
    }

    @Override
    public void start() throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(host);
        for (int i = 0; i < X; i++) {
            System.out.println(String.format("client #%d, iter %d: start", id, i));
            UDPSerialCommunicator.communicate(socket, address, port, N);
            if (i != X - 1) {
                try {
                    TimeUnit.MILLISECONDS.sleep(DELTA_MILLIS);} catch (InterruptedException ignored) {}
            }
            System.out.println(String.format("client #%d, iter %d: OK", id, i));
        }
    }
}
