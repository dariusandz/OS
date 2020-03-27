package com.mif.vm;

public class VirtualMachine {

    private VirtualProcessor virtualProcessor;
    public VirtualMemory virtualMemory;

    public VirtualMachine() {
        virtualMemory = new VirtualMemory();
        this.virtualProcessor = new VirtualProcessor(virtualMemory);
    }

    public void run() {
        virtualProcessor.runProgram();
    }

    public void loadProgram(String filename) {
        virtualProcessor.loadInstructionsFromFile(filename);
    }

    public void freeMemory() {
        virtualMemory.freeMemory();
    }
}
