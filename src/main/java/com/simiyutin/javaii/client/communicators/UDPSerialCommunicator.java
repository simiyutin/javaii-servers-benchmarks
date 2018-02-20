package com.simiyutin.javaii.client.communicators;

import com.google.protobuf.InvalidProtocolBufferException;
import com.simiyutin.javaii.client.MessageFactory;
import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.proto.SerializationWrapper;
import com.simiyutin.javaii.server.SortAlgorithm;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UDPSerialCommunicator {
    public static boolean communicate(DatagramSocket socket, byte[] buffer, InetAddress address, int port, int arraySize) throws IOException {
        // request
        MessageProtos.Message message = MessageFactory.createMessage(arraySize);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        SerializationWrapper.serialize(message, baos);
        byte[] sendData = baos.toByteArray();
        System.arraycopy(sendData, 0, buffer, 0, sendData.length);
        DatagramPacket sendPacket = new DatagramPacket(buffer, buffer.length, address, port);
        socket.send(sendPacket);

        // response
        DatagramPacket receivePacket = new DatagramPacket(buffer, buffer.length);
        try {
            socket.receive(receivePacket);
        } catch (SocketTimeoutException ex) {
            return false;
        }

        ByteArrayInputStream bais = new ByteArrayInputStream(buffer);
        try {
            MessageProtos.Message response = SerializationWrapper.deserialize(bais);
            List<Integer> result = response.getArrayList();
            SortAlgorithm.checkSorted(message.getArrayList(), result);
        } catch (InvalidProtocolBufferException ex) {
            System.out.println("invalid protobuf message, trying again");
            return false;
        } catch (AssertionError ex) {
            System.out.println("unexpected message, trying again");
            return false;
        }

        return true;
    }
}
