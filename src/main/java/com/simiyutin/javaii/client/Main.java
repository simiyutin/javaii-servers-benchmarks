package com.simiyutin.javaii.client;

import com.simiyutin.javaii.server.SerialServer;
import com.simiyutin.javaii.server.Server;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;

public class Main {
    public static void main(String[] args) throws IOException {
        String host = "localhost";
        int port = 11111;

        new Thread(() -> {
            try {
                Server server = new SerialServer(port);
                server.start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }).start();

        try {
            TimeUnit.MILLISECONDS.sleep(200);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }


        SerialClient client = new SerialClient();
        client.start(host, port);
        System.out.println("all right!");
    }
}
