package com.simiyutin.javaii.server.nonblocking;

import com.simiyutin.javaii.server.SortAlgorithm;

import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.*;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class SocketChannelsProcessor implements Runnable {
    private final Queue<SocketChannel> channelsQueue;
    private final Queue<Result> resultQueue;
    private final Selector readSelector;
    private final Selector writeSelector;
    private final ExecutorService threadPool;


    public SocketChannelsProcessor(Queue<SocketChannel> channelsQueue) throws IOException {
        this.channelsQueue = channelsQueue;
        this.resultQueue = new ArrayBlockingQueue<>(1024);
        this.readSelector = Selector.open();
        this.writeSelector = Selector.open();
        this.threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        while (true) {
            try{
                executeNonBlockingCycle();
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

    private void executeNonBlockingCycle() throws IOException {
        registerNewChannels();
        readFromChannels();

        registerReadyTasks();
        writeToChannels();
    }

    private void registerNewChannels() throws IOException {
        SocketChannel channel = channelsQueue.poll();
        while (channel != null) {
            channel.configureBlocking(false);
            SelectionKey key = channel.register(readSelector, SelectionKey.OP_READ);
            key.attach(new SocketChannelReader());
            channel = channelsQueue.poll();
        }
    }

    private void readFromChannels() throws IOException {
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
        for (List<Integer> message : fullMessages) {
            // не нужно упорядочивать ответы, потому что клиент шлет запрос и ждет
            threadPool.submit(() -> {
                SortAlgorithm.sort(message);
                resultQueue.add(new Result(message, channel));
            });
        }
    }

    private void registerReadyTasks() throws ClosedChannelException {
        Result result = resultQueue.poll();

        while (result != null) {
            List<Integer> data = result.data;
            SocketChannel channel = result.channel;
            SelectionKey key = channel.register(writeSelector, SelectionKey.OP_WRITE);
            if (key.attachment() == null) {
                key.attach(new SocketChannelWriter());
            }
            SocketChannelWriter writer = (SocketChannelWriter) key.attachment();
            writer.addData(data);

            result = resultQueue.poll();
        }
    }

    private void writeToChannels() throws IOException {
        int writeReady = writeSelector.selectNow();
        if (writeReady > 0) {
            Set<SelectionKey> keys = writeSelector.selectedKeys();
            Iterator<SelectionKey> it = keys.iterator();
            while (it.hasNext()) {
                SelectionKey key = it.next();
                writeToSocket(key);
                it.remove();
            }
        }
    }

    private void writeToSocket(SelectionKey key) throws IOException {
        SocketChannel channel = (SocketChannel) key.channel();
        SocketChannelWriter writer = (SocketChannelWriter) key.attachment();
        Runnable unsubscribeHook = key::cancel;
        writer.write(channel, unsubscribeHook);
    }

}
