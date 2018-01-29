package com.simiyutin.javaii.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class SerialServer implements Server {
    private final ServerSocket serverSocket;

    public SerialServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

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

    private void handleTCP(Socket socket) throws IOException {
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        int val = dis.readInt();
        System.out.println(String.format("hello! val=%d", val));

        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeInt(val);
    }
}
