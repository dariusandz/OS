package com.mif.rm;

public enum DeviceType {
    BATTERY, LED;

    @Override
    public String toString() {
        return this == BATTERY ? "Battery" : "LED";
    }
}
