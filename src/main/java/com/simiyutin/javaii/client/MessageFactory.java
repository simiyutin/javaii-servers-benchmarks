package com.simiyutin.javaii.client;

import com.simiyutin.javaii.proto.MessageProtos;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MessageFactory {
    public static MessageProtos.Message createMessage(int arraySize) {
        List<Integer> array = IntStream.range(0, arraySize).
                map(i -> ~i).sorted().map(i -> ~i) // reverse order
                .boxed().collect(Collectors.toList());

        return MessageProtos.Message.newBuilder().addAllArray(array).build();
    }
}
