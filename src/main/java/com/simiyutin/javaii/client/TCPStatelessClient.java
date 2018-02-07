package com.simiyutin.javaii.client;

import com.simiyutin.javaii.Configuration;
import com.simiyutin.javaii.client.communicators.TCPSerialCommunicator;
import com.simiyutin.javaii.proto.MessageProtos;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TCPStatelessClient extends Client {

    public TCPStatelessClient(String host, int port, Configuration conf) {
        super(host, port, conf);
    }

    @Override
    public void startImpl() throws IOException {
        for (int i = 0; i < conf.clientNumberOfRequests; i++) {
            Socket socket = new Socket(host, port);
            TCPSerialCommunicator.communicate(socket, conf.clientArraySize);
            if (i != conf.clientNumberOfRequests - 1) {
                try {TimeUnit.MILLISECONDS.sleep(conf.clientDeltaMillis);} catch (InterruptedException ignored) {}
            }
            System.out.println(String.format("iter %d: OK", i));
            socket.close();
        }

    }
}
