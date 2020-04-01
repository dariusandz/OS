package com.mif.FXModel;

import com.mif.rm.Device;
import com.mif.rm.DeviceState;
import com.mif.rm.DeviceType;

public class DeviceTableRow {

    private Long id;
    private DeviceType type;
    private int value;
    private DeviceState state;

    public DeviceTableRow(Long id, DeviceType type, int value, DeviceState state) {
        this.id = id;
        this.type = type;
        this.value = value;
        this.state = state;
    }

    public DeviceTableRow(Device device) {
        this.id = device.getId();
        this.type = device.getType();
        this.value = device.getValue();
        this.state = device.getState();
    }

    public Long getId() {
        return id;
    }

    public String isType() {
        return type.toString();
    }

    public DeviceType getType() {
        return type;
    }

    public void setType(DeviceType type) {
        this.type = type;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String isState() {
        return state.toString();
    }

    public DeviceState getState() {
        return state;
    }

    public void setState(DeviceState state) {
        this.state = state;
    }
}
