package com.mif.rm;

import com.mif.vm.VirtualMemory;
import javafx.util.Pair;

import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class Processor {

    private static Processor processor = getInstance();

    public static ProcessorMode processorMode;
    public static Register PTR, AX, BX;
    public static Register IC, PI, SI, TI, PR, SP, MODE, ES, DI, ZF;
    public static List<Device> devices;

    public static Processor getInstance() {
        if (processor == null)
            return new Processor();

        return processor;
    }

    private Processor() {
        this.devices = new ArrayList<>();
        this.processorMode = ProcessorMode.SUPERVISOR;
        initializeRegisters();
    }

    private Processor(ProcessorMode mode) { this.processorMode = mode; }

    private void initializeRegisters() {
        PTR = new Register();
        AX = new Register();
        BX = new Register();
        IC = new Register();
        PI = new Register();
        SI = new Register();
        TI = new Register();
        PR = new Register();
        SP = new Register();
        MODE = new Register();
        ES = new Register();
        DI = new Register();
        ZF = new Register();
    }

    public Pair<Integer, String> processSIValue(VirtualMemory virtualMemory) {
        switch (SI.getValue()) {
            case 1:
                int letterCount = BX.getValue();
                return new Pair<>(1, "Type in " + letterCount + " symbols");
            case 2:
                byte[] bytes1 = virtualMemory.getBytesFromMemory(AX.getByteValue()[2], AX.getByteValue()[3], BX.getValue());
                return new Pair<>(2, new String(bytes1, StandardCharsets.UTF_8));
            case 3:
                return new Pair<>(3, null);
            case 4:
                return new Pair<>(4, new String(virtualMemory.getWordFromMemory(AX.getByteValue()[2], AX.getByteValue()[3]), StandardCharsets.UTF_8));
            case 5:
                int fileNamePageNum = Processor.AX.getByteValue()[2];
                int fileNameWordNum = Processor.AX.getByteValue()[3];
                byte[] temp;
                int byteCount = 1;
                while (true){
                    temp = virtualMemory.getBytesFromMemory(fileNamePageNum, fileNameWordNum, byteCount);
                    if(temp[byteCount-1] == 0)
                        break;
                    byteCount++;
                    if(fileNameWordNum + byteCount/4 == 16) {
                        fileNameWordNum = 0;
                        fileNamePageNum++;
                    }
                }
                return new Pair<>(5, new String(temp));
            case 6:
                if(devices.size() < AX.getValue()) {
                    return new Pair<>(6, "Error: Bad device index");
                }
                else {
                    switch (BX.getValue()) {
                        case 0:
                        case 1:
                            devices.get(
                                    AX.getValue() - 1).onOffSwitch(
                                            DeviceState.parseState(BX.getValue())
                            );
                            break;
                        case 2:
                            devices.get(AX.getValue() - 1).onOffSwitch();
                            break;
                        case 3:
                            processor.BX.setValue(
                                    devices.get(processor.AX.getValue() - 1).getState().toInt()
                            );
                            break;
                        default:
                            return new Pair<>(6, "Error: Unknown device command");
                    }
                }
                break;
            case 7:
                if(devices.size() < AX.getValue()) {
                    return new Pair<>(7, "Error: Bad device index");
                }
                else {
                    if (devices.get(processor.AX.getValue() - 1).getState() == DeviceState.ON)
                        devices.get(processor.AX.getValue() - 1).setValue(processor.BX.getValue());
                    else {
                        return new Pair<>(7, "Error: Turn on the device");
                    }
                }
                break;
            case 8:
                if(devices.size() < AX.getValue()) {
                    return new Pair<>(8, "Error: Bad device index");
                }
                else {
                    if (devices.get(processor.AX.getValue() - 1).getState() == DeviceState.ON){
                        int deviceValue = devices.get(processor.AX.getValue() - 1).getValue();
                        BX.setValue(deviceValue);
                    }
                    else {
                        return new Pair<>(8, "Error: Turn on the device");
                    }
                }
                break;
            case 9:
                if(devices.size() < AX.getValue()) {
                    return new Pair<>(9, "Error: Bad device index");
                }
                else {
                    if (devices.get(processor.AX.getValue() - 1).getState() == DeviceState.ON)
                        processor.BX.setValue(
                                devices.get(processor.AX.getValue() - 1).getIntType()
                        );
                    else {
                        return new Pair<>(9, "Error: Turn on the device");
                    }
                }
                break;
            case 10: // MONT
                if (processor.AX.getValue() == 1)
                    devices.add(new Device(DeviceType.BATTERY));
                else if (processor.AX.getValue() == 2)
                    devices.add(new Device(DeviceType.LED));
                else {
                    return new Pair<>(10, "Error: Bad device type");
                }
                break;
            case 11:
                if(devices.size() < AX.getValue()) {
                    return new Pair<>(10, "Error: Bad device index");
                }
                else {
                    Device deviceToRemove = devices.get(AX.getValue() - 1);
                    devices.remove(deviceToRemove);
                    return new Pair<>(11, deviceToRemove.getId().toString());
                }
            default:
                SI.setValue(0);
                return null;
        }
        SI.setValue(0);
        return null;
    }

    String processTIValue() {
        devices.forEach(device ->
                device.tick()
        );

        if(TI.getValue() <= 0) {
            return "Error: Timer is over";
        }
        return null;
    }

    String processPIValue() {
        switch (Processor.PI.getValue()) {
            case 0:
                return null;
            case 1:
                return "Error: Wrong address";
            case 2:
                return "Error: Wrong operation code";
            case 3:
                return "Error: Wrong assignment";
            case 4:
                return "Error: Overflow";
        }
        return null;
    }

    public void resetRegisterValues() {
        initializeRegisters();
    }

    public void setRegister(Field regField, byte[] value) {
        Field[] fields = this.getClass().getDeclaredFields();
        for (Field field : fields) {
            if (field.getType().equals(Register.class)) {
                field.setAccessible(true);
                if (regField.getName().equals(field.getName())) {
                    try {
                        ((Register) field.get(processor)).setValue(value);
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
