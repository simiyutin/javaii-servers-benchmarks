package com.simiyutin.javaii.testarch;

import com.simiyutin.javaii.client.Client;
import com.simiyutin.javaii.client.TCPStatefulClient;
import com.simiyutin.javaii.client.TCPStatelessClient;
import com.simiyutin.javaii.server.Server;
import com.simiyutin.javaii.server.tcp_async.TCPAsyncServer;
import com.simiyutin.javaii.server.tcp_nonblocking.TCPNonBlockingServer;
import com.simiyutin.javaii.server.tcp_serial.TCPSerialServer;
import com.simiyutin.javaii.server.tcp_threadperclient.TCPThreadPerClientServer;
import com.simiyutin.javaii.server.tcp_threadpool.TCPThreadPoolServer;

import java.util.function.Supplier;

public class ApplicationConfigurationFactory {
    public static ClientServer getConfiguration(String serverType, Configuration conf) {
        Supplier<Client> clientSupplier = null;
        Supplier<Server> serverSupplier = null;

        switch (serverType) {
            case "tcp_serial":
                clientSupplier = () -> new TCPStatelessClient(conf);
                serverSupplier = () -> new TCPSerialServer(conf.port);
                break;
            case "tcp_threadperclient":
                clientSupplier = () -> new TCPStatefulClient(conf);
                serverSupplier = () -> new TCPThreadPerClientServer(conf.port);
                break;
            case "tcp_threadpool":
                clientSupplier = () -> new TCPStatefulClient(conf);
                serverSupplier = () -> new TCPThreadPoolServer(conf.port);
                break;
            case "tcp_nonblocking":
                clientSupplier = () -> new TCPStatefulClient(conf);
                serverSupplier = () -> new TCPNonBlockingServer(conf.port);
                break;
            case "tcp_async":
                clientSupplier = () -> new TCPStatefulClient(conf);
                serverSupplier = () -> new TCPAsyncServer(conf.port);
                break;
        }

        return new ClientServer(clientSupplier, serverSupplier);
    }
}
