package com.mif.rm;

public enum DeviceState {
    ON, OFF;

    public int toInt() {
        return this == ON ? 1 : 0;
    }

    public static DeviceState parseState(int state) {
        return state == 1 ? ON : OFF;
    }

    @Override
    public String toString() {
        return this == ON ? "On" : "Off";
    }
}
