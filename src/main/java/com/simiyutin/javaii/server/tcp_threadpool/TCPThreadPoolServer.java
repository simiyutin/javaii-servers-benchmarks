package com.simiyutin.javaii.server.tcp_threadpool;

import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.handlers.TCPSerialHandler;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TCPThreadPoolServer implements Server {
    private final ServerSocket serverSocket;
    private final ExecutorService threadPool;

    public TCPThreadPoolServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port);
        this.threadPool = Executors.newCachedThreadPool();
    }

    @Override
    public void start() {
        while (true) {
            try {
                Socket socket = serverSocket.accept();

                // "Клиент устанавливает постоянное соединение.
                //  Сервер использует кеширующий пул потоков для общения с конкретными клиентами.
                //  Задача для этого пула потоков - общение с клиентом, пока не разорвется соединение."
                // Больше всего под описание подходит делать IO в тредпуле, пусть это и страшно
                threadPool.submit(() -> {
                    while (true) {
                        try {
                            TCPSerialHandler.handle(socket);
                        } catch (IOException e) {
                            e.printStackTrace();
                            return; // todo отследить, когда именно отвалится соединение, а не что либо еще.
                            // Или не делать этого, тк у нас задача на тестирование архитектуры, а не на крутое проганье
                        }
                    }
                });

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
