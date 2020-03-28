package com.mif.rm;

import com.mif.common.Register;
import com.mif.vm.CMD;
import com.mif.vm.VirtualMachine;
import com.mif.vm.VirtualMemory;

import java.nio.charset.StandardCharsets;
import java.util.Scanner;

import static java.lang.Integer.parseInt;

public class Processor {

    private static Processor processor = null;

    public ProcessorMode processorMode;
    static VirtualMemory virtualMemory;
    public Register PTR, AX, BX;
    public Register IC, PI, SI, TI, PR, SP, MODE, ES, DI, ZF;
    Memory memory;

    public static Processor getInstance() {
        if (processor == null)
            return new Processor();

        return processor;
    }

    Processor() {
        this.processorMode = ProcessorMode.SUPERVISOR;
        initializeRegisters();
        memory = memory.getInstance();
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

    void run() {
        int outputText;
        Scanner scan = new Scanner(System.in);
        while (true) {
            System.out.println("Type in the number for which virtual machine to start");
            outputText = parseInt(scan.nextLine());
            switch (outputText) {
                case 1:
                    VirtualMachine vm = new VirtualMachine();
                    vm.loadProgram("/pr1.txt");
                    virtualMemory = vm.virtualMemory;
                    vm.run();
                    vm.freeMemory();
                    break;
                default:

            }

        }
    }

    public static boolean processSIValue(Processor processor) {
        switch (processor.SI.getValue()) { //TODO needs to be redone?
            case 1:
                int letterCount = processor.BX.getValue();
                byte[] address = processor.AX.getByteValue();
                String outputText = "";
                Scanner scan = new Scanner(System.in);
                while (outputText.length() != letterCount) {
                    System.out.println("Type in " + letterCount + " symbols");
                    outputText = scan.nextLine();
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
                // TODO device controller

                break;
            case 7:
                break;
            case 8:
                break;
            case 9:
                break;
            default:
                processor.SI.setValue(0);
                return true;
        }
        return true;
    }
}
