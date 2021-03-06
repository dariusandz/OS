package com.mif.rm;

import com.mif.common.ByteUtil;

import java.awt.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class Register {

    private static final int size = 4;

    private byte[] value = new byte[size];

    public Register() { Arrays.fill(value, (byte) 0); }

    public Register(int initialValue) {
        this.value = ByteBuffer.allocate(this.size).putInt(initialValue).array();
    }

    public int getSize() {
        return size;
    }

    public int getValue() {
        return ByteBuffer.wrap(this.value).getInt();
    }

    public byte[] getByteValue() { return this.value; }

    public void setValue(int value) {
        this.value = ByteBuffer.allocate(this.size).putInt(value).array();
    }

    public void setValue(byte[] bytes) { this.value = bytes; }

    public void incrementValue(int by) {
        int value = this.getValue();
        value += by;
        this.setValue(value);
    }

    public void incrementValue() {
        int value = this.getValue();
        this.setValue(++value);
    }

    public String getHexValue() {
        return ByteUtil.bytesToHex(value);
    }

    public int getValueOfSmallerTwoBytes() {
        return ByteUtil.byteToInt(new byte[] {0, 0, value[2], value[3]});
    }
}
