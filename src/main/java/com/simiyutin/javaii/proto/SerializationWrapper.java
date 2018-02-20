package com.simiyutin.javaii.proto;

import com.google.protobuf.CodedOutputStream;

import java.io.*;

public class SerializationWrapper {
    public static int serialize(MessageProtos.Message message, OutputStream os) throws IOException {
        byte[] array = message.toByteArray();
        new DataOutputStream(os).writeInt(array.length);
        os.write(array);
        return array.length + 4;
    }

    public static MessageProtos.Message deserialize(InputStream is) throws IOException {
        int size = new DataInputStream(is).readInt();
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
