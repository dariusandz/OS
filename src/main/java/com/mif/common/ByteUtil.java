package com.mif.common;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class ByteUtil {

    public static int stringHexToInt(String word) {
        byte[] b =  word.getBytes();
        return ByteBuffer.wrap(b).getInt();
    }

    public static byte[] intToBytes(int value) {
        return ByteBuffer.allocate(4).putInt(value).array();
    }

    public static byte getIthByteFromString(String word, int i) {
        return word.getBytes()[i];
    }

    public static byte[] minifyHex(String hex) {
        byte[] hexBytes = new byte[hex.length() / 2];
        for (int i = 0; i < hex.length(); i += 2) {
            hexBytes[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i+1), 16));
        }
        return hexBytes;
    }

    public static List<Byte> appendBytesToByteList(List<Byte> byteList, byte[] bytes) {
        for (byte aByte : bytes) {
            byteList.add((Byte) aByte);
        }
        return byteList;
    }

    public static byte[] getByteArrayFromByteList(List<Byte> byteList) {
        byte[] bytes = new byte[byteList.size()];
        for(int i = 0; i < bytes.length; i++)
            bytes[i] = byteList.get(i).byteValue();
        return bytes;
    }
}
