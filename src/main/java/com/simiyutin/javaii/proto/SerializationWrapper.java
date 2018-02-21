package com.simiyutin.javaii.proto;

import com.simiyutin.javaii.statistics.ServeStatistic;

import java.io.*;

public class SerializationWrapper {

    public static int serialize(MessageProtos.Message message, OutputStream os) throws IOException {
        return serializeGeneral(message, os, null);
    }

    public static int serialize(MessageProtos.Message message, OutputStream os, ServeStatistic serveStatistic) throws IOException {
        return serializeGeneral(message, os, serveStatistic);
    }

    private static int serializeGeneral(MessageProtos.Message message, OutputStream os, ServeStatistic serveStatistic) throws IOException {
        byte[] array = message.toByteArray();
        new DataOutputStream(os).writeInt(array.length);
        os.write(array);
        if (serveStatistic != null) {
            serveStatistic.setEndTime();
        }
        return array.length + 4;
    }

    public static MessageProtos.Message deserialize(InputStream is) throws IOException {
        return deserializeGeneral(is, null);
    }

    public static MessageProtos.Message deserialize(InputStream is, ServeStatistic serveStatistic) throws IOException {
        return deserializeGeneral(is, serveStatistic);
    }

    private static MessageProtos.Message deserializeGeneral(InputStream is, ServeStatistic serveStatistic) throws IOException {
        int size = new DataInputStream(is).readInt();
        if (serveStatistic != null) {
            serveStatistic.setStartTime();
        }
        byte[] array = new byte[size];
        int offset = 0;
        int read;
        while ((offset < size)) {
            read = is.read(array, offset, size - offset);
            offset += read;
        }
        return MessageProtos.Message.parseFrom(array);
    }
}
