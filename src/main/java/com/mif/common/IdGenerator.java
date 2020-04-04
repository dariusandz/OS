package com.mif.common;

public class IdGenerator {

    private static Long vmId = Long.valueOf(0);
    private static Long deviceId = Long.valueOf(0);

    public Long getVmId() {
        return vmId++;
    }

    public Long getDeviceId() { return deviceId++; }
}
