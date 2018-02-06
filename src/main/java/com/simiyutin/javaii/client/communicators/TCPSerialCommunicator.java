package com.simiyutin.javaii.client.communicators;

import com.simiyutin.javaii.proto.MessageProtos;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TCPSerialCommunicator {
    public static void communicate(Socket socket, int arraySize) throws IOException {
        // request
        List<Integer> array = IntStream.range(0, arraySize).
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
