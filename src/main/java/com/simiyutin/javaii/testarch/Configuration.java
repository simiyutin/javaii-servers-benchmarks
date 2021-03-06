package com.simiyutin.javaii.testarch;

public class Configuration {
    // количество элементов в сортируемых массивах
    public int clientArraySize;
    // время, которое клиент ждет между запросами
    public int clientDeltaMillis;
    // количество запросов от одного клиента
    public int clientNumberOfRequests;
    // количество одновременно работающих клиентов
    public int numberOfClients;

    public String host;
    public int port;
}
