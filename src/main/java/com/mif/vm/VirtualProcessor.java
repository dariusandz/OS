package com.mif.vm;

public class VirtualProcessor extends CommandProcessor {

    public VirtualProcessor(VirtualMemory memory) {
        super(memory);
    }

    public boolean loadInstructionsFromFile(String fileName) {
        return memory.loadProgram(fileName);
    }

    String getCommand() {
        return new String(memory.getCodeWord(processor.IC.getValue()));
    }

}
