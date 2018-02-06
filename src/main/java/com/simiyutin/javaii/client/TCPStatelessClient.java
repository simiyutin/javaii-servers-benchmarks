package com.simiyutin.javaii.client;

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

    public TCPStatelessClient(String host, int port) {
        super(host, port);
    }

    public void start() throws IOException {
        for (int i = 0; i < X; i++) {
            Socket socket = new Socket(host, port);
            TCPSerialCommunicator.communicate(socket, N);
            if (i != X - 1) {
                try {TimeUnit.MILLISECONDS.sleep(DELTA_MILLIS);} catch (InterruptedException ignored) {}
            }
            System.out.println(String.format("iter %d: OK", i));
        }

    }
}
