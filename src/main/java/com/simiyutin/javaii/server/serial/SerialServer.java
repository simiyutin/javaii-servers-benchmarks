package com.simiyutin.javaii.server.serial;

import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.SortAlgorithm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SerialServer extends Server {
    private final ServerSocket serverSocket;

    public SerialServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    @Override
    public void start() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                handleTCP(socket);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // up
    private void handleTCP(Socket socket) throws IOException {
        //read
        MessageProtos.Message request = MessageProtos.Message.parseDelimitedFrom(socket.getInputStream());
        List<Integer> array = new ArrayList<>(request.getArrayList());

        //process
        SortAlgorithm.sort(array);

        MessageProtos.Message response = MessageProtos.Message.newBuilder().addAllArray(array).build();

        response.writeDelimitedTo(socket.getOutputStream());
    }
}
