package com.simiyutin.javaii.server.tcp_nonblocking;

import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.server.SortAlgorithm;
import com.simiyutin.javaii.statistics.ServerServeTimeStatistic;
import com.simiyutin.javaii.statistics.ServerSortTimeStatistic;

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
    private boolean stopped = false;
    private final List<ServerSortTimeStatistic> sortTimeStatistics;
    private final List<ServerServeTimeStatistic> serveTimeStatistics;
    private final Set<SelectionKey> cancelledKeys;
    private final Set<SelectionKey> resurrectedKeys;

    public SocketChannelsProcessor(Queue<SocketChannel> channelsQueue, List<ServerSortTimeStatistic> sortTimeStatistics, List<ServerServeTimeStatistic> serveTimeStatistics) throws IOException {
        this.channelsQueue = channelsQueue;
        this.resultQueue = new ArrayBlockingQueue<>(1024);
        this.readSelector = Selector.open();
        this.writeSelector = Selector.open();
        this.threadPool = Executors.newCachedThreadPool();
        this.sortTimeStatistics = sortTimeStatistics;
        this.serveTimeStatistics = serveTimeStatistics;
        this.cancelledKeys = new HashSet<>();
        this.resurrectedKeys = new HashSet<>();
    }

    @Override
    public void run() {
        while (!readyToStop()) {
            try{
                executeNonBlockingCycle();
            } catch(IOException e){
                e.printStackTrace();
            }

            try {
                TimeUnit.MILLISECONDS.sleep(100);
            } catch (InterruptedException e) {
                System.out.println("processor closed!");
                stopped = true;
            }
        }
        threadPool.shutdown();
    }

    private boolean readyToStop() {
        return stopped && readSelector.selectedKeys().isEmpty() && resultQueue.isEmpty();
    }

    private void executeNonBlockingCycle() throws IOException {
        if (!stopped) {
            registerNewChannels();
        }
        readFromChannels();
        registerReadyTasks();
        resurrectKeys();
        cancelKeys();
        writeToChannels();
    }

    private void resurrectKeys() {
        for (SelectionKey key : resurrectedKeys) {
            cancelledKeys.remove(key);
        }
        resurrectedKeys.clear();
    }

    private void cancelKeys() {
        for (SelectionKey key : cancelledKeys) {
            System.out.println("key cancelled!");
            key.cancel();
        }
        cancelledKeys.clear();
    }

    private void registerNewChannels() throws IOException {
        SocketChannel channel = channelsQueue.poll();
        while (channel != null) {
            System.out.println("new channel!");
            channel.configureBlocking(false);
            SelectionKey key = channel.register(readSelector, SelectionKey.OP_READ);
            key.attach(new SocketChannelReaderProtobuf());
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
        SocketChannelReaderProtobuf reader = (SocketChannelReaderProtobuf) key.attachment();
        reader.read(channel);
        List<MessageProtos.Message> fullMessages = reader.getFullMessages();

        if (fullMessages.size() > 0) {
            System.out.println("got full messages!");
        }
        for (MessageProtos.Message message : fullMessages) {
            long startTime = System.currentTimeMillis();
            threadPool.submit(() -> {
                List<Integer> array = new ArrayList<>(message.getArrayList());
                long sortTime = SortAlgorithm.sort(array);
                sortTimeStatistics.add(new ServerSortTimeStatistic(sortTime));
                MessageProtos.Message response = MessageProtos.Message.newBuilder().addAllArray(array).build();
                resultQueue.add(new Result(response, channel, startTime));
            });
        }
    }

    private void registerReadyTasks() throws ClosedChannelException {
        Result result = resultQueue.poll();

        while (result != null) {
            long endTime = System.currentTimeMillis();
            serveTimeStatistics.add(new ServerServeTimeStatistic(endTime - result.startTime));

            MessageProtos.Message message = result.message;
            SocketChannel channel = result.channel;
            SelectionKey key = channel.keyFor(writeSelector);
            if (key == null) {
                System.out.println("new key!");
                key = channel.register(writeSelector, SelectionKey.OP_WRITE);
                key.attach(new SocketChannelWriter());
            }
            resurrectedKeys.add(key);
            if (!key.isValid()) {
                System.out.println("oooooops!!!");
            }
            SocketChannelWriter writer = (SocketChannelWriter) key.attachment();
            writer.addMessage(message);

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
        Runnable unsubscribeHook = () -> cancelledKeys.add(key);;
        writer.write(channel, unsubscribeHook);
    }

    private static class Result {
        MessageProtos.Message message;
        SocketChannel channel;
        long startTime;

        public Result(MessageProtos.Message message, SocketChannel channel, long startTime) {
            this.message = message;
            this.channel = channel;
            this.startTime = startTime;
        }
    }
}
