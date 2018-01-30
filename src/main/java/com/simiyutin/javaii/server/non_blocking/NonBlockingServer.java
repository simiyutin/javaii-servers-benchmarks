package com.simiyutin.javaii.server.non_blocking;

import com.simiyutin.javaii.server.Server;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Queue;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class NonBlockingServer extends Server {

    private final ServerSocketChannel serverSocket;
    private final Queue<SocketChannel> channelsQueue;
    private final int CHANNELS_QUEUE_CAPACITY = 1024;

    public NonBlockingServer(int port) throws IOException {
        this.serverSocket = ServerSocketChannel.open();
        this.serverSocket.bind(new InetSocketAddress(port));
        this.channelsQueue = new ArrayBlockingQueue<>(CHANNELS_QUEUE_CAPACITY);
    }

    @Override
    public void start() throws IOException {

        SocketChannelsProcessor channelsProcessor = new SocketChannelsProcessor(channelsQueue);

        new Thread(channelsProcessor).start();

        while (true) {
            try {
                SocketChannel socket = serverSocket.accept();
                channelsQueue.add(socket);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
