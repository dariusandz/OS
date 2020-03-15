package com.mif.vm;

public class VirtualMachine {

    private VirtualProcessor virtualProcessor;

    public VirtualMachine() {
        this.virtualProcessor = new VirtualProcessor(new VirtualMemory());
    }

    public void run() {
        virtualProcessor.runProgram();
    }

    public void loadProgram(String filename) {
        virtualProcessor.loadInstructionsFromFile(filename);
    }
}
