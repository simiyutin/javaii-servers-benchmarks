package com.simiyutin.javaii.server.tcp_serial;

import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.handlers.TCPSerialHandler;
import com.simiyutin.javaii.statistics.*;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class TCPSerialServer extends Server {
    private final int port;
    private ServerSocket serverSocket;
    private Thread listener;

    public TCPSerialServer(int port) {
        this.port = port;
    }

    @Override
    public void start() throws IOException {
        this.serverSocket = new ServerSocket(port);
        listener = new Thread(() -> {
            while (!Thread.interrupted() && !serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    SortStatistic sortStatistic = new SortStatistic();
                    ServeStatistic serveStatistic = new ServeStatistic();
                    TCPSerialHandler.handle(socket, sortStatistic, serveStatistic);
                    socket.close();
                    sortStatistics.add(sortStatistic);
                    serveStatistics.add(serveStatistic);
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
