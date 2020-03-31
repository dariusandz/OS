package com.mif.vm;

import com.mif.rm.Processor;

import java.util.List;

public class VirtualMachine {

    public VirtualProcessor virtualProcessor;
    public VirtualMemory virtualMemory;

    public VirtualMachine(List<String> params) {
        this.virtualMemory = new VirtualMemory(params);
        this.virtualProcessor = new VirtualProcessor(virtualMemory);
    }

    public String getCommand() {
        return virtualProcessor.getCommand();
    }

    public Processor processCommand(String command) {
        return virtualProcessor.processCommand(command);
    }

    public void loadProgram(String filename) {
        virtualProcessor.loadInstructionsFromFile(filename);
    }

    public void freeMemory() {
        virtualMemory.freeMemory();
    }
}
