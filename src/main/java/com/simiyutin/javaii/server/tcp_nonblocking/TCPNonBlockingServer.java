package com.simiyutin.javaii.server.tcp_nonblocking;

import com.simiyutin.javaii.server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ClosedByInterruptException;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;

public class TCPNonBlockingServer extends Server {
    private ServerSocketChannel serverSocket;
    private final Queue<SocketChannel> channelsQueue;
    private final int CHANNELS_QUEUE_CAPACITY = 1024;
    private Thread channelsProcessor;
    private Thread listener;
    private final int port;

    public TCPNonBlockingServer(int port) {
        this.port = port;
        this.channelsQueue = new ArrayBlockingQueue<>(CHANNELS_QUEUE_CAPACITY);
    }

    @Override
    public void start() throws IOException {
        this.serverSocket = ServerSocketChannel.open();
        this.serverSocket.bind(new InetSocketAddress(port));
        channelsProcessor = new Thread(new SocketChannelProcessor(channelsQueue, sortStatistics, serveStatistics));
        channelsProcessor.start();

        listener = new Thread(() -> {
            while (!Thread.interrupted() && serverSocket.isOpen()) {
                try {
                    SocketChannel socket = serverSocket.accept();
                    channelsQueue.add(socket);
                }
                catch (ClosedByInterruptException ex) {
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
        if (channelsProcessor != null) {
            channelsProcessor.interrupt();
        }

        if (listener != null) {
            listener.interrupt();
        }

        try {
            serverSocket.close();
        } catch (IOException ignored) {}
    }
}
