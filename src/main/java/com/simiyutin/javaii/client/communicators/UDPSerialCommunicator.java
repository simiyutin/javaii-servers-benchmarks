package com.simiyutin.javaii.client.communicators;

import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.proto.SerializationWrapper;
import com.simiyutin.javaii.server.SortAlgorithm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UDPSerialCommunicator {
    public static void communicate(DatagramSocket socket, InetAddress address, int port, int arraySize) throws IOException {
        // request
        List<Integer> array = IntStream.range(0, arraySize).
                map(i -> ~i).sorted().map(i -> ~i) // reverse order
                .boxed().collect(Collectors.toList());

        MessageProtos.Message message = MessageProtos.Message.newBuilder().addAllArray(array).build();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        SerializationWrapper.serialize(message, baos);
        byte[] sendData = baos.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
        socket.send(sendPacket);

        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);
        ByteArrayInputStream bais = new ByteArrayInputStream(receiveData);
        MessageProtos.Message response = SerializationWrapper.deserialize(bais);
        List<Integer> result = response.getArrayList();
        SortAlgorithm.checkSorted(array, result);
    }
}
