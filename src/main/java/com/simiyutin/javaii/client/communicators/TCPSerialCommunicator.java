package com.simiyutin.javaii.client.communicators;

import com.simiyutin.javaii.client.MessageFactory;
import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.proto.SerializationWrapper;
import com.simiyutin.javaii.server.SortAlgorithm;

import java.io.IOException;
import java.net.Socket;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class TCPSerialCommunicator {
    public static void communicate(Socket socket, int arraySize) throws IOException {
        // request
        MessageProtos.Message message = MessageFactory.createMessage(arraySize);
        SerializationWrapper.serialize(message, socket.getOutputStream());

        // response
        MessageProtos.Message result = SerializationWrapper.deserialize(socket.getInputStream());
        List<Integer> resultArray = result.getArrayList();
        SortAlgorithm.checkSorted(message.getArrayList(), resultArray);
    }
}
