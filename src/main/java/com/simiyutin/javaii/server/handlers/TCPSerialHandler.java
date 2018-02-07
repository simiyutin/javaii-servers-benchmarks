package com.simiyutin.javaii.server.handlers;

import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.proto.SerializationWrapper;
import com.simiyutin.javaii.server.SortAlgorithm;
import com.simiyutin.javaii.statistics.ServerSortTimeStatistic;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPSerialHandler {
    public static long handle(Socket socket) throws IOException {
        //read
        MessageProtos.Message request = SerializationWrapper.deserialize(socket.getInputStream());
        if (request == null) { // todo google it
            return 0;
        }
        List<Integer> array = new ArrayList<>(request.getArrayList());

        //process
        long sortTime = SortAlgorithm.sort(array);

        //write
        MessageProtos.Message response = MessageProtos.Message.newBuilder().addAllArray(array).build();
        SerializationWrapper.serialize(response, socket.getOutputStream());
        return sortTime;
    }
}
