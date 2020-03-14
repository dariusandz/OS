package com.mif.common;

import java.nio.ByteBuffer;

public class Util {

    public static int stringBytesToInt(String word) {
        byte[] b =  word.getBytes();
        return ByteBuffer.wrap(b).getInt();
    }
}
