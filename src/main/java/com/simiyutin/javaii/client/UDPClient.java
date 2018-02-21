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
        int timeout = Math.abs(new Random(id).nextInt() % 10000);
        socket.setSoTimeout(timeout);
        InetAddress address = InetAddress.getByName(conf.host);
        for (int i = 0; i < conf.clientNumberOfRequests;) {
            boolean succesfullyCommunicated = UDPSerialCommunicator.communicate(socket, buffer, address, conf.port, conf.clientArraySize);
            if (!succesfullyCommunicated) {
                System.out.println("client: retry");
                continue;
            }
            if (i != conf.clientNumberOfRequests - 1) {
                try {
                    TimeUnit.MILLISECONDS.sleep(conf.clientDeltaMillis);} catch (InterruptedException ignored) {}
            }
            i++;
        }
    }
}
