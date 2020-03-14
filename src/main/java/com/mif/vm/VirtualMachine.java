package com.mif.vm;

public class VirtualMachine {

    private VirtualMemory virtualMemory;
    private VirtualProcessor processor;

    public VirtualMachine() {
        this.virtualMemory = new VirtualMemory();
        this.processor = new VirtualProcessor(virtualMemory);
    }

    public void run() {
        processor.runProgram();
    }

    public void loadInstructionsFromFile(String filename) {
        virtualMemory.loadProgram(filename);
    }
}
