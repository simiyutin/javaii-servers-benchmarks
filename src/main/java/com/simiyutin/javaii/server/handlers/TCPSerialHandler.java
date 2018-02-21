package com.simiyutin.javaii.server.handlers;

import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.proto.SerializationWrapper;
import com.simiyutin.javaii.server.SortAlgorithm;
import com.simiyutin.javaii.statistics.ServeStatistic;
import com.simiyutin.javaii.statistics.ServerServeTimeStatistic;
import com.simiyutin.javaii.statistics.ServerSortTimeStatistic;
import com.simiyutin.javaii.statistics.SortStatistic;

import java.io.IOException;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class TCPSerialHandler {
    // todo make void
    public static long handle(Socket socket, SortStatistic sortStatistic, ServeStatistic serveStatistic) throws IOException {
        //read
        MessageProtos.Message request = SerializationWrapper.deserialize(socket.getInputStream(), serveStatistic);
        if (request == null) {
            return 0;
        }
        // make mutable
        List<Integer> array = new ArrayList<>(request.getArrayList());

        //process
        sortStatistic.setStartTime();
        long sortTime = SortAlgorithm.sort(array);
        sortStatistic.setEndTime();

        //write
        MessageProtos.Message response = MessageProtos.Message.newBuilder().addAllArray(array).build();
        SerializationWrapper.serialize(response, socket.getOutputStream(), serveStatistic);
        return sortTime;
    }
}
