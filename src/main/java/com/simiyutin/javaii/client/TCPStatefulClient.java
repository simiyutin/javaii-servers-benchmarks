package com.simiyutin.javaii.client;

import com.simiyutin.javaii.client.communicators.TCPSerialCommunicator;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class TCPStatefulClient extends Client {

    public TCPStatefulClient(String host, int port) {
        super(host, port);
    }

    @Override
    public void start() throws IOException {
        Socket socket = new Socket(host, port);
        for (int i = 0; i < X; i++) {
            TCPSerialCommunicator.communicate(socket, N);
            if (i != X - 1) {
                try {
                    TimeUnit.MILLISECONDS.sleep(DELTA_MILLIS);} catch (InterruptedException ignored) {}
            }
            System.out.println(String.format("iter %d: OK", i));
        }
        socket.close();
    }
}
