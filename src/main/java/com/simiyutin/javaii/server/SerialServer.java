package com.simiyutin.javaii.server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Arrays;

public class SerialServer implements Server {
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
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        int n = dis.readInt();
        int[] array = new int[n];
        for (int i = 0; i < n; i++) {
            array[i] = dis.readInt();
        }

        //process
        sort(array);

        //write
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeInt(n);

        for (int val : array) {
            dos.writeInt(val);
        }
    }

    // up
    private void sort(int[] array) {
        Arrays.sort(array);
    }
}
