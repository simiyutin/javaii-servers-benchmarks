package com.simiyutin.javaii.client;

import java.io.IOException;

public interface Client {
    // количество элементов в сортируемых массивах
    int N = 10;
    // время, которое клиент ждет между запросами
    int DELTA_MILLIS = 200;
    // количество запросов
    int X = 10;

    void start() throws IOException;
}
