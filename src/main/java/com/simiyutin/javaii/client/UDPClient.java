package com.simiyutin.javaii.client;

import com.simiyutin.javaii.proto.MessageProtos;
import com.simiyutin.javaii.proto.SerializationWrapper;

import java.io.*;
import java.net.*;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class UDPClient implements Client {

    private String host;
    private int port;
    private static int gid = 0;
    private int id;

    public UDPClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.id = gid++;
    }

    @Override
    public void start() throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(host);
        for (int i = 0; i < X; i++) {
            System.out.println(String.format("client #%d, iter %d: start", id, i));
            communicate(socket, address, port);
            if (i != X - 1) {
                try {
                    TimeUnit.MILLISECONDS.sleep(DELTA_MILLIS);} catch (InterruptedException ignored) {}
            }
            System.out.println(String.format("client #%d, iter %d: OK", id, i));
        }
    }

    private void communicate(DatagramSocket socket, InetAddress address, int port) throws IOException {
        // request
        List<Integer> array = IntStream.range(0, N).
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
        for (int i = 1; i < result.size(); ++i) {
            if (result.get(i - 1) > result.get(i)) {
                throw new AssertionError("azaza lalka");
            }
        }
    }
}
