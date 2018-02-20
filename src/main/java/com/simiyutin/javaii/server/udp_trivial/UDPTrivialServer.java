package com.simiyutin.javaii.server.udp_trivial;

import com.simiyutin.javaii.server.Server;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.SocketException;

public class UDPTrivialServer extends Server{
    private DatagramSocket serverSocket;
    private Thread listener;
    private final int port;
    private final byte[] buffer;

    //todo передавать сюда conf и вычислять размер пакета
    public UDPTrivialServer(int port) {
        this.port = port;
        this.buffer = new byte[60000];
    }

    @Override
    public void start() throws IOException {
        this.serverSocket = new DatagramSocket(port);
        listener = new Thread(() -> {
            while (true) {
                try {
                    DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
                    serverSocket.receive(receivePacket);
                    DatagramPacket responsePacket = new DatagramPacket(buffer, buffer.length, receivePacket.getAddress(), receivePacket.getPort()); // todo ???
                    serverSocket.send(responsePacket);
                }
                catch (SocketException ex) {
                    return;
                }
                catch (IOException ex) {
                    ex.printStackTrace();
                }
            }
        });
        listener.start();
    }

    @Override
    public void stop() {
        if (listener != null) {
            listener.interrupt();
        }
        serverSocket.close();
    }
}
