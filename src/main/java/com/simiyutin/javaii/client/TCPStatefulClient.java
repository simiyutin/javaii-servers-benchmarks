package com.simiyutin.javaii.client;

import com.simiyutin.javaii.Configuration;
import com.simiyutin.javaii.client.communicators.TCPSerialCommunicator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class TCPStatefulClient extends Client {

    public TCPStatefulClient(String host, int port, Configuration conf) {
        super(host, port, conf);
    }

    @Override
    public void startImpl() throws IOException {
        Socket socket = new Socket(host, port);
        for (int i = 0; i < conf.clientNumberOfRequests; i++) {
            TCPSerialCommunicator.communicate(socket, conf.clientArraySize);
            if (i != conf.clientNumberOfRequests - 1) {
                try {
                    TimeUnit.MILLISECONDS.sleep(conf.clientDeltaMillis);} catch (InterruptedException ignored) {}
            }
            System.out.println(String.format("iter %d: OK", i));
        }
        socket.close();
    }
}
