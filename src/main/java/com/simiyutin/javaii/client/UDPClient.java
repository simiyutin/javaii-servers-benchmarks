package com.simiyutin.javaii.client;

import com.simiyutin.javaii.testarch.Configuration;
import com.simiyutin.javaii.client.communicators.UDPSerialCommunicator;

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;

public class UDPClient extends Client {
    private static int gid = 0;
    private int id;

    public UDPClient(String host, int port, Configuration conf) {
        super(host, port, conf);
        this.id = gid++;
    }

    @Override
    public void startImpl() throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(host);
        for (int i = 0; i < conf.clientNumberOfRequests; i++) {
//            System.out.println(String.format("client #%d, iter %d: startt", id, i));
            UDPSerialCommunicator.communicate(socket, address, port, conf.clientArraySize);
            if (i != conf.clientNumberOfRequests - 1) {
                try {
                    TimeUnit.MILLISECONDS.sleep(conf.clientDeltaMillis);} catch (InterruptedException ignored) {}
            }
//            System.out.println(String.format("client #%d, iter %d: OK", id, i));
        }
    }
}
