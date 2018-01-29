package com.simiyutin.javaii.server;

import java.util.Arrays;

public abstract class Server {
    public abstract void start();

    protected void sort(int[] array) {
        Arrays.sort(array);
    }
}
