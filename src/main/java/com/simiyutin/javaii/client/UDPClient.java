package com.simiyutin.javaii.client;

import com.google.protobuf.InvalidProtocolBufferException;
import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.proto.SerializationWrapper;
import com.simiyutin.javaii.server.SortAlgorithm;
import com.simiyutin.javaii.testarch.Configuration;
import com.simiyutin.javaii.client.communicators.UDPSerialCommunicator;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;

public class UDPClient extends Client {
    private static int gid = 0;
    private int id;
    private final byte[] buffer;

    public UDPClient(Configuration conf) {
        super(conf);
        this.id = gid++;
        this.buffer = new byte[60000];
    }

    @Override
    public void startImpl() throws IOException {
        DatagramSocket socket = new DatagramSocket();
        int timeout = Math.abs(new Random(id).nextInt() % 1000);
        socket.setSoTimeout(timeout);
        InetAddress address = InetAddress.getByName(conf.host);
        for (int i = 0; i < conf.clientNumberOfRequests;) {
            System.out.println(String.format("client #%d, iter %d: startt", id, i));
            boolean succesfullyCommunicated = UDPSerialCommunicator.communicate(socket, buffer, address, conf.port, conf.clientArraySize);
            if (!succesfullyCommunicated) {
                continue;
            }
            if (i != conf.clientNumberOfRequests - 1) {
                try {
                    TimeUnit.MILLISECONDS.sleep(conf.clientDeltaMillis);} catch (InterruptedException ignored) {}
            }
            System.out.println(String.format("client #%d, iter %d: OK", id, i));
            i++;
        }
    }
}
