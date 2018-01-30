package com.simiyutin.javaii.client;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class TCPStatefulClient implements Client {

    private String host;
    private int port;

    public TCPStatefulClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws IOException {
        Socket socket = new Socket(host, port);
        for (int i = 0; i < X; i++) {
            communicate(socket);
            if (i != X - 1) {
                try {
                    TimeUnit.MILLISECONDS.sleep(DELTA_MILLIS);} catch (InterruptedException ignored) {}
            }
            System.out.println(String.format("iter %d: OK", i));
        }
        socket.close();
    }

    // TODO REMOVE CLONE
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
        // todo check if it is the same array but sorted
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
