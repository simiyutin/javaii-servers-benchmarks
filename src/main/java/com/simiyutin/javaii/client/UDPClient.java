package com.simiyutin.javaii.client;

import java.io.*;
import java.net.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.IntStream;

public class UDPClient implements Client {

    private String host;
    private int port;

    public UDPClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    @Override
    public void start() throws IOException {
        DatagramSocket socket = new DatagramSocket();
        InetAddress address = InetAddress.getByName(host);
        for (int i = 0; i < X; i++) {
            communicate(socket, address, port);
            if (i != X - 1) {
                try {
                    TimeUnit.MILLISECONDS.sleep(DELTA_MILLIS);} catch (InterruptedException ignored) {}
            }
            System.out.println(String.format("iter %d: OK", i));
        }
    }

    private void communicate(DatagramSocket socket, InetAddress address, int port) throws IOException {
        // request
        int[] array = IntStream.range(0, N).
                map(i -> ~i).sorted().map(i -> ~i) // reverse order
                .toArray();

        ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
        DataOutputStream dos = new DataOutputStream(baos);
        dos.writeInt(array.length);
        for (int val : array) {
            dos.writeInt(val);
        }
        byte[] sendData = baos.toByteArray();
        DatagramPacket sendPacket = new DatagramPacket(sendData, sendData.length, address, port);
        socket.send(sendPacket);

        byte[] receiveData = new byte[1024];
        DatagramPacket receivePacket = new DatagramPacket(receiveData, receiveData.length);
        socket.receive(receivePacket);
        DataInputStream dis = new DataInputStream(new ByteArrayInputStream(receiveData));
        int n = dis.readInt();
        int predVal = dis.readInt();
        for (int i = 1; i < n; i++) {
            int curVal = dis.readInt();
            if (curVal < predVal) {
                throw new AssertionError("azaza lalka");
            }
            predVal = curVal;
        }
    }
}
