package com.mif.vm;

import com.mif.rm.Processor;

public class VirtualProcessor extends CommandProcessor {

    public VirtualProcessor(VirtualMemory memory) {
        super(memory);
    }

    public void loadInstructionsFromFile(String fileName) {
        memory.loadProgram(fileName);
    }

    String getCommand() {
        return new String(memory.getCodeWord(processor.IC.getValue()));
    }

}
