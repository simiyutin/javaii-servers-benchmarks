package com.simiyutin.javaii.server;

import java.io.IOException;
import java.util.Arrays;

public abstract class Server {
    public abstract void start() throws IOException;

    protected void sort(int[] array) {
        Arrays.sort(array);
    }
}
