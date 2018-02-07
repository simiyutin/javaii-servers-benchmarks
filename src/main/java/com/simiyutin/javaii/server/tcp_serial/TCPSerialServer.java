package com.simiyutin.javaii.server.tcp_serial;

import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.handlers.TCPSerialHandler;
import com.simiyutin.javaii.statistics.ServerServeTimeStatistic;
import com.simiyutin.javaii.statistics.ServerSortTimeStatistic;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class TCPSerialServer extends Server {
    private final ServerSocket serverSocket;
    private Thread listener;

    public TCPSerialServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    @Override
    public void start() {
        listener = new Thread(() -> {
            while (!Thread.interrupted() && !serverSocket.isClosed()) {
                try {
                    long start = System.currentTimeMillis();
                    Socket socket = serverSocket.accept();
                    ServerSortTimeStatistic sortTimeStatistic = TCPSerialHandler.handle(socket);
                    sortTimeStatistics.add(sortTimeStatistic);
                    socket.close();
                    long end = System.currentTimeMillis();
                    ServerServeTimeStatistic serveTimeStatistic = new ServerServeTimeStatistic();
                    serveTimeStatistic.timeMillis = end - start;
                    serveTimeStatistics.add(serveTimeStatistic);
                }
                catch (SocketException ignored) {
                    return;
                }
                catch (IOException e) {
                    e.printStackTrace();
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

        try {
            serverSocket.close();
        } catch (IOException ignored) { }
    }
}
