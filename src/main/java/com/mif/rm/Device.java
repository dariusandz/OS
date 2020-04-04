package com.mif.rm;

import com.mif.common.IdGenerator;

public class Device {

    private IdGenerator generator = new IdGenerator();

    private Long id;
    private final DeviceType type;
    private int value;
    private DeviceState state;

    public Device(DeviceType type) {
        this.id = generator.getDeviceId();
        this.type = type;
        this.value = 10;
        this.state = DeviceState.OFF;
    }

    public int getIntType() {
        return type == DeviceType.BATTERY ? 1 : 2;
    }

    public DeviceType getType() {
        return type;
    }

    public Long getId() {
        return id;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public void onOffSwitch(DeviceState state) {
        this.state = state;
    }

    public void onOffSwitch() {
        if (this.state == DeviceState.OFF)
            this.state = DeviceState.ON;
        else this.state = DeviceState.OFF;
    }

    public DeviceState getState() {
        return this.state;
    }

    public void tick() {
        switch (this.type) {
            case BATTERY:
                if (this.state == DeviceState.OFF)
                    this.value += 2;
                else this.value -= 2;
                break;
        }
    }
}
