package com.mif.rm;

public class Device {
    private final int type;
    private int value;
    private int power;

    public Device(int type) {
        this.type = type;
        this.value = 10;
        this.power = 0;
    }

    public int getType() {
        return type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void onOffSwitch(int power) {
        this.power = power;
    }

    public void onOffSwitch() {
        if(this.power == 0)
            this.power = 1;
        else this.power = 0;
    }

    public int getPower() {
        return this.power;
    }
}
