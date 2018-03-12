package com.simiyutin.javaii.client;

import com.simiyutin.javaii.proto.MessageProtos;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class MessageFactory {
    public static MessageProtos.Message createMessage(int arraySize) {
        List<Integer> array = new Random().ints().boxed().limit(arraySize).collect(Collectors.toList());
        return MessageProtos.Message.newBuilder().addAllArray(array).build();
    }
}
