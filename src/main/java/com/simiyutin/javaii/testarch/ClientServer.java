package com.simiyutin.javaii.testarch;

import com.simiyutin.javaii.client.Client;
import com.simiyutin.javaii.server.Server;

import java.util.function.Supplier;

public class ClientServer {
    private Supplier<Client> clientSupplier;
    private Supplier<Server> serverSupplier;

    public ClientServer(Supplier<Client> clientSupplier, Supplier<Server> serverSupplier) {
        this.clientSupplier = clientSupplier;
        this.serverSupplier = serverSupplier;
    }

    public Supplier<Client> getClientSupplier() {
        return clientSupplier;
    }

    public Supplier<Server> getServerSupplier() {
        return serverSupplier;
    }
}
