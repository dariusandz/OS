package com.mif.common;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

public class Util {

    public static int stringBytesToInt(String word) {
        byte[] b =  word.getBytes();
        return ByteBuffer.wrap(b).getInt();
    }

    public static byte[] intToBytes(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static String bytesToString(byte[] word) {
        return new String(word, StandardCharsets.UTF_8);
    }

    public static byte getIthByteFromString(String word, int i) {
        return word.getBytes()[i];
    }
}
