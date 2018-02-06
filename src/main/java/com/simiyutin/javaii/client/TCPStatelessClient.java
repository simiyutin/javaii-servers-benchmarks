package com.simiyutin.javaii.client;

import com.simiyutin.javaii.proto.MessageProtos;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TCPStatelessClient implements Client {
    private String host;
    private int port;

    public TCPStatelessClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws IOException {
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
        List<Integer> array = IntStream.range(0, N).
                map(i -> ~i).sorted().map(i -> ~i) // reverse order
                .boxed().collect(Collectors.toList());

        MessageProtos.Message message = MessageProtos.Message.newBuilder()
                .addAllArray(array)
                .build();
        message.writeDelimitedTo(socket.getOutputStream());

        // response
        MessageProtos.Message result = MessageProtos.Message.parseDelimitedFrom(socket.getInputStream());
        List<Integer> resultArray = result.getArrayList();
        for (int i = 1; i < resultArray.size(); ++i) {
            if (resultArray.get(i - 1) > resultArray.get(i)) {
                throw new AssertionError("azaza lalka");
            }
        }
    }
}
