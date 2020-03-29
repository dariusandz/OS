package com.mif.vm;

import java.util.ArrayList;
import java.util.List;

public class VirtualMachine {

    public VirtualProcessor virtualProcessor;
    public VirtualMemory virtualMemory;

    public VirtualMachine(List<String> params) {
        this.virtualMemory = new VirtualMemory(params);
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
