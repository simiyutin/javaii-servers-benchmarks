package com.simiyutin.javaii.server.handlers;

import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.server.SortAlgorithm;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPSerialHandler {
    public static void handle(Socket socket) throws IOException {
        //read
        MessageProtos.Message request = MessageProtos.Message.parseDelimitedFrom(socket.getInputStream());
        if (request == null) { // todo google it
            return;
        }
        List<Integer> array = new ArrayList<>(request.getArrayList());

        //process
        SortAlgorithm.sort(array);

        MessageProtos.Message response = MessageProtos.Message.newBuilder().addAllArray(array).build();

        response.writeDelimitedTo(socket.getOutputStream());
    }
}
