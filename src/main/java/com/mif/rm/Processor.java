package com.mif.rm;

import com.mif.exception.FatalInterruptException;
import com.mif.exception.HarmlessInterruptException;
import com.mif.exception.InterruptException;
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
    public static Register IC, PI, SI, TI, PR, SP, MODE, DS, DI, ZF;
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
        DS = new Register();
        DI = new Register();
        ZF = new Register();
    }

    public Pair<Integer, String> processSIValue(VirtualMemory virtualMemory) {
        int SIValue = SI.getValue();
        SI.setValue(0);
        switch (SIValue) {
            case 1:
                int letterCount = BX.getValue();
                return new Pair<>(SIValue, "Type in " + letterCount + " symbols");
            case 2:
                byte[] bytes1 = virtualMemory.getBytesFromMemory(AX.getByteValue()[2], AX.getByteValue()[3], BX.getValue());
                return new Pair<>(SIValue, new String(bytes1, StandardCharsets.UTF_8));
            case 3:
                throw new FatalInterruptException("Virtuali masina baige darba.", "SI: " + SI.getValue());
            case 4:
                return new Pair<>(SIValue, new String(virtualMemory.getWordFromMemory(AX.getByteValue()[2], AX.getByteValue()[3]), StandardCharsets.UTF_8));
            case 5:
                int fileNamePageNum = Processor.AX.getByteValue()[2];
                int fileNameWordNum = Processor.AX.getByteValue()[3];
                byte[] temp;
                int byteCount = 1;
                while (true) {
                    temp = virtualMemory.getBytesFromMemory(fileNamePageNum, fileNameWordNum, byteCount);
                    if (temp[byteCount-1] == 0)
                        break;
                    byteCount++;
                    if (fileNameWordNum + byteCount/4 == 16) {
                        fileNameWordNum = 0;
                        fileNamePageNum++;
                    }
                }
                return new Pair<>(SIValue, new String(temp));
            case 6:
                if (devices.size() < AX.getValue()) {
                    throw new HarmlessInterruptException("Klaida: blogas irenginio indeksas", "SI: " + SI.getValue());
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
                            throw new HarmlessInterruptException("Klaida: nezinoma irenginio komanda", "SI: " + SI.getValue());
                    }
                }
                break;
            case 7:
                if (devices.size() < AX.getValue()) {
                    throw new HarmlessInterruptException("Klaida: blogas irenginio indeksas", "SI: " + SI.getValue());
                }
                else {
                    if (devices.get(processor.AX.getValue() - 1).getState() == DeviceState.ON)
                        devices.get(processor.AX.getValue() - 1).setValue(processor.BX.getValue());
                    else {
                        throw new HarmlessInterruptException("Klaida: pirmiau reiktu ijungti irengini..",  "SI: " + SI.getValue());
                    }
                }
                break;
            case 8:
                if (devices.size() < AX.getValue()) {
                    throw new HarmlessInterruptException("Klaida: blogas irenginio indeksas", "SI: " + SI.getValue());
                }
                else {
                    if (devices.get(processor.AX.getValue() - 1).getState() == DeviceState.ON) {
                        int deviceValue = devices.get(processor.AX.getValue() - 1).getValue();
                        BX.setValue(deviceValue);
                    }
                    else {
                        throw new HarmlessInterruptException("Klaida: pirmiau reiktu ijungti irengini..",  "SI: " + SI.getValue());
                    }
                }
                break;
            case 9:
                if (devices.size() < AX.getValue()) {
                    throw new HarmlessInterruptException("Klaida: blogas irenginio indeksas", "SI: " + SI.getValue());
                }
                else {
                    if (devices.get(processor.AX.getValue() - 1).getState() == DeviceState.ON)
                        processor.BX.setValue(
                                devices.get(processor.AX.getValue() - 1).getIntType()
                        );
                    else {
                        throw new HarmlessInterruptException("Klaida: pirmiau reiktu ijungti irengini..",  "SI: " + SI.getValue());
                    }
                }
                break;
            case 10: // MONT
                if (processor.AX.getValue() == 1)
                    devices.add(new Device(DeviceType.BATTERY));
                else if (processor.AX.getValue() == 2)
                    devices.add(new Device(DeviceType.LED));
                else {
                    throw new HarmlessInterruptException("Klaida: blogas irenginio tipas", "SI: " + SI.getValue());
                }
                break;
            case 11:
                if (devices.size() < AX.getValue()) {
                    throw new HarmlessInterruptException("Klaida: blogas irenginio indeksas", "SI: " + SI.getValue());
                } else {
                    Device deviceToRemove = devices.get(AX.getValue() - 1);
                    devices.remove(deviceToRemove);
                    return new Pair<>(SIValue, deviceToRemove.getId().toString());
                }
            default:
                return null;
        }
        return null;
    }

    public void processTIValue() {
        devices.forEach(device ->
                device.tick()
        );

        if (TI.getValue() <= 0) {
            throw new FatalInterruptException("Klaida: baigesi taimerio laikas", "TI: " + TI.getValue());
        }
    }

    public void processPIValue() {
        switch (Processor.PI.getValue()) {
            case 1:
                throw new FatalInterruptException("Klaida: neteisingas adresas", "PI: " + PI.getValue());
            case 2:
                throw new FatalInterruptException("Klaida: neteisingas operacijos kodas", "PI: " + PI.getValue());
            case 3:
                throw new FatalInterruptException("Klaida: neteisingas priskyrimas", "PI: " + PI.getValue());
            case 4:
                throw new FatalInterruptException("Klaida: perpildymas (overflow)", "PI: " + PI.getValue());
            default:
                return;
        }
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
