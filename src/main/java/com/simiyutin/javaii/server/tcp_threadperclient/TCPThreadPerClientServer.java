package com.simiyutin.javaii.server.tcp_threadperclient;

import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.handlers.TCPSerialHandler;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

public class TCPThreadPerClientServer extends Server {
    private final ServerSocket serverSocket;
    private Thread listener;

    public TCPThreadPerClientServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
    }

    @Override
    public void start() {
        listener = new Thread(() -> {
            while (!Thread.interrupted() && !serverSocket.isClosed()) {
                try {
                    Socket socket = serverSocket.accept();
                    new Thread(() -> {
                        while (!socket.isClosed()) {
                            try {
                                TCPSerialHandler.handle(socket);
                            }
                            catch (EOFException ex) {
                                return;
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();

                }
                catch (SocketException ex) {
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
        } catch (IOException ignored) {}
    }
}
