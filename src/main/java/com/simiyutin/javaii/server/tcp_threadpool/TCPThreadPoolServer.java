package com.simiyutin.javaii.server.tcp_threadpool;

import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.handlers.TCPSerialHandler;
import com.simiyutin.javaii.statistics.ServerServeTimeStatistic;
import com.simiyutin.javaii.statistics.ServerSortTimeStatistic;

import java.io.EOFException;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPThreadPoolServer extends Server {
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;
    private Thread listener;

    public TCPThreadPoolServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void start() {
        listener = new Thread(() -> {
            while (!Thread.interrupted()) {
                try {
                    Socket socket = serverSocket.accept();

                    // "Клиент устанавливает постоянное соединение.
                    //  Сервер использует кеширующий пул потоков для общения с конкретными клиентами.
                    //  Задача для этого пула потоков - общение с клиентом, пока не разорвется соединение."
                    // Больше всего под описание подходит делать IO в тредпуле, пусть это и страшно
                    threadPool.submit(() -> {
                        while (true) {
                            try {
                                long sortTime = TCPSerialHandler.handle(socket);
                                sortTimeStatistics.add(new ServerSortTimeStatistic(sortTime));
                                serveTimeStatistics.add(new ServerServeTimeStatistic(sortTime));
                            }
                            catch (EOFException ex) {
                                return;
                            }
                            catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    });

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
    public void stop(){
        if (listener != null) {
            listener.interrupt();
        }
        try {
            serverSocket.close();
        } catch (IOException e) {}

        threadPool.shutdown();
    }
}
