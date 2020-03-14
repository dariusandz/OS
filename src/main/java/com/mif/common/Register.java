package com.mif.common;

import java.nio.ByteBuffer;

public class Register {

    private static final int size = 4;

    private byte[] value = new byte[size];

    public Register() { }

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
}
