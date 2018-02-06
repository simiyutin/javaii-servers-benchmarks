package com.simiyutin.javaii.server.serial;

import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.SortAlgorithm;
import com.simiyutin.javaii.server.handlers.TCPSerialHandler;

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
                TCPSerialHandler.handle(socket);
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
