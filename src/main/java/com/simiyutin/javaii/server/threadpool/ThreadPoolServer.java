package com.simiyutin.javaii.server.threadpool;

import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.SortAlgorithm;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ThreadPoolServer extends Server {
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;

    public ThreadPoolServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void start() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();

                threadPool.submit(() -> {
                    while (true) {
                        try {
                            handleTCP(socket);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return; // todo отследить, когда именно отвалится соединение, а не что либо еще.
                            // Или не делать этого, тк у нас задача на тестирование архитектуры, а не на крутое проганье
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void handleTCP(Socket socket) throws IOException {
        //read
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        int n = dis.readInt();
        int[] array = new int[n];
        for (int i = 0; i < n; i++) {
            array[i] = dis.readInt();
        }

        //process
        SortAlgorithm.sort(array);

        //write
        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeInt(n);

        for (int val : array) {
            dos.writeInt(val);
        }
    }
}
