package com.simiyutin.javaii.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class SerialClient {
    // количество элементов в сортируемых массивах
    private static final int N = 10;
    // время, которое клиент ждет между запросами
    private static final int DELTA_MILLIS = 200;
    // количество запросов
    private static final int X = 10;

    public void start(String host, int port) throws IOException {
        for (int i = 0; i < X; i++) {
            Socket socket = new Socket(host, port);
            communicate(socket);
            if (i != X - 1) {
                try {TimeUnit.MILLISECONDS.sleep(DELTA_MILLIS);} catch (InterruptedException ignored) {}
            }
            System.out.println(String.format("iter %d: OK", i));
        }

    }

    private void communicate(Socket socket) throws IOException {
        // request
        int[] array = IntStream.range(0, N).
                map(i -> ~i).sorted().map(i -> ~i) // reverse order
                .toArray();

        DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
        dos.writeInt(array.length);
        for (int anArray : array) {
            dos.writeInt(anArray);
        }

        // response
        DataInputStream dis = new DataInputStream(socket.getInputStream());
        int n = dis.readInt();
        int predVal = dis.readInt();
        for (int i = 1; i < n; i++) {
            int curVal = dis.readInt();
            if (curVal < predVal) {
                throw new AssertionError("azaza lalka");
            }
            predVal = curVal;
        }
    }
}
