package com.simiyutin.javaii.client;

import java.io.IOException;

public abstract class Client {
    // количество элементов в сортируемых массивах
    public static final int N = 10;
    // время, которое клиент ждет между запросами
    public static final int DELTA_MILLIS = 200;
    // количество запросов
    public static final int X = 10;

    protected String host;
    protected int port;

    public Client(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public abstract void start() throws IOException;
}
