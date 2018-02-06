package com.simiyutin.javaii.server.tcp_threadperclient;

import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.handlers.TCPSerialHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class TCPThreadPerClientServer implements Server {
    private final ServerSocket serverSocket;

    public TCPThreadPerClientServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    @Override
    public void start() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();
                new Thread(() -> {
                    while (true) {
                        try {
                            TCPSerialHandler.handle(socket);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                }).start();

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
