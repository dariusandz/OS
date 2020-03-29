package com.mif.rm;

import com.mif.common.Register;
import com.mif.vm.CMD;
import com.mif.vm.VirtualMachine;
import com.mif.vm.VirtualMemory;

import javax.swing.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import static java.lang.Integer.parseInt;

public class Processor {

    private static Processor processor = null;

    public ProcessorMode processorMode;
    public Register PTR, AX, BX;
    public Register IC, PI, SI, TI, PR, SP, MODE, ES, DI, ZF;
    static List<Device> devices;
    private Memory memory;

    public static Processor getInstance() {
        if (processor == null)
            return new Processor();

        return processor;
    }

    private Processor() {
        this.memory = memory.getInstance();
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

//    void run() {
//        int outputText;
////        Scanner scan = new Scanner(System.in);
//        while (true) {
//            System.out.println("Type in the number for which virtual machine to start");
//            outputText = 1;//parseInt(scan.nextLine());
//            switch (outputText) {
//                case 1:
//                    VirtualMachine vm = new VirtualMachine();
//                    vm.loadProgram("/pr1.txt");
//                    vm.run();
//                    vm.freeMemory();
//                    break;
//                case 2:
//                    VirtualMachine vm2 = new VirtualMachine();
//                    vm2.loadProgram("/pr2.txt");
//                    vm2.run();
//                    vm2.freeMemory();
//                    break;
//                default:
//
//            }
//        }
//    }

    public static boolean processSIValue(Processor processor, VirtualMemory virtualMemory) {
        switch (processor.SI.getValue()) { //TODO needs to be redone?
            case 1:
                int letterCount = processor.BX.getValue();
                byte[] address = processor.AX.getByteValue();
                String outputText = "1234567";
                Scanner scan = new Scanner(System.in);
                while (outputText.length() != letterCount) {
                    System.out.println("Type in " + letterCount + " symbols");
//                    outputText = scan.nextLine();
                }
                byte[] bytes = outputText.getBytes(StandardCharsets.UTF_8);
                virtualMemory.putBytesToMemory(address[2], address[3], bytes, bytes.length);
                break;
            case 2:
                byte[] bytes1 = virtualMemory.getBytesFromMemory(processor.AX.getByteValue()[2], processor.AX.getByteValue()[3], processor.BX.getValue());
                System.out.println(new String(bytes1, StandardCharsets.UTF_8));
                break;
            case 3:
                return false;
            case 4:
                System.out.println(new String(virtualMemory.getWordFromMemory(processor.AX.getByteValue()[2], processor.AX.getByteValue()[3]), StandardCharsets.UTF_8));
                break;
            case 5:
                // TODO LOAD command
                break;
            case 6:
                if(devices.size() < processor.AX.getValue()) {
                    System.out.println("Bad device index");
                    return false;
                }
                else {
                    switch (processor.BX.getValue()) {
                        case 0:
                            devices.get(processor.AX.getValue()-1).onOffSwitch(processor.BX.getValue());
                            break;
                        case 1:
                            devices.get(processor.AX.getValue()-1).onOffSwitch(processor.BX.getValue());
                            break;
                        case 2:
                            devices.get(processor.AX.getValue()-1).onOffSwitch();
                            break;
                        case 3:
                            processor.BX.setValue(devices.get(processor.AX.getValue()-1).getPower());
                            break;
                        default:
                            System.out.println("Unknown device command");
                            return false;
                    }
                }
                break;
            case 7:
                if(devices.size() < processor.AX.getValue()) {
                    System.out.println("Bad device index");
                    return false;
                }
                else {
                    if (devices.get(processor.AX.getValue() - 1).getPower() == 1)
                        devices.get(processor.AX.getValue() - 1).setValue(processor.BX.getValue());
                    else {
                        System.out.println("Turn on the device");
                        return false;
                    }
                }
                break;
            case 8:
                if(devices.size() < processor.AX.getValue()) {
                    System.out.println("Bad device index");
                    return false;
                }
                else {
                    if (devices.get(processor.AX.getValue() - 1).getPower() == 1){
                        int deviceValue = devices.get(processor.AX.getValue() - 1).getValue();
                        if(deviceValue == 10)
                            processor.BX.setValue(new byte[] {0, 0, '1', '0'} );
                        else
                            processor.BX.setValue(new byte[] {0, 0, 0, (byte) (deviceValue+'0')});
                    }
                    else {
                        System.out.println("Turn on the device");
                        return false;
                    }
                }
                break;
            case 9:
                if(devices.size() < processor.AX.getValue()) {
                    System.out.println("Bad device index");
                    return false;
                }
                else {
                    if (devices.get(processor.AX.getValue() - 1).getPower() == 1)
                        processor.BX.setValue(devices.get(processor.AX.getValue() - 1).getType());
                    else {
                        System.out.println("Turn on the device");
                        return false;
                    }
                }
                break;
            case 10: // MONT
                if (processor.AX.getValue() == 1)
                    devices.add(new Device(1));
                else if (processor.AX.getValue() == 2)
                    devices.add(new Device(2));
                else {
                    System.out.println("Bad device type");
                    return false;
                }
                break;
            case 11:
                if(devices.size() < processor.AX.getValue()) {
                    System.out.println("Bad device index");
                    return false;
                }
                else devices.remove(processor.AX.getValue() - 1);
                break;
            default:
                processor.SI.setValue(0);
                return true;
        }
        processor.SI.setValue(0);
        return true;
    }
}
