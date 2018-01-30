package com.simiyutin.javaii.server.non_blocking;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.TimeUnit;

public class SocketChannelsProcessor implements Runnable {
    private final Queue<SocketChannel> channelsQueue;
    private final Selector readSelector;
    private final Selector writeSelector;


    public SocketChannelsProcessor(Queue<SocketChannel> channelsQueue) throws IOException {
        this.channelsQueue = channelsQueue;
        this.readSelector = Selector.open();
        this.writeSelector = Selector.open();
    }

    @Override
    public void run() {
        while (true) {
            try{
                executeCycle();
            } catch(IOException e){
                e.printStackTrace();
            }

            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private void executeCycle() throws IOException {
        registerNewChannnel();
        readFromSockets();
    }

    private void registerNewChannnel() throws IOException {
        SocketChannel channel = channelsQueue.poll();
        if (channel != null) {
            channel.configureBlocking(false);
            SelectionKey key = channel.register(readSelector, SelectionKey.OP_READ);
            key.attach(new SocketChannelReader());
        }
    }

    private void readFromSockets() throws IOException {
        int readReady = readSelector.selectNow();
        if (readReady > 0) {
            Set<SelectionKey> keys = readSelector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                readFromSocket(key);
                it.remove();
            }
        }
    }

    private void readFromSocket(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        SocketChannelReader reader = (SocketChannelReader) key.attachment();
        reader.read(channel);
        List<List<Integer>> fullMessages = reader.getFullMessages();
        if (fullMessages.size() > 0) {
            System.out.println("got messages!");
            System.out.println(fullMessages);
        }
    }

}
