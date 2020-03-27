package com.mif.vm;

import com.mif.rm.Processor;

public class VirtualProcessor extends CommandProcessor {

    public VirtualProcessor(VirtualMemory memory) {
        super(memory);
    }

    public void runProgram() {
        while (true){
            String command = getCommand();
            processCommand(command);
            if(!(Processor.processSIValue(processor)))
                break;
        }
    }

    public void loadInstructionsFromFile(String fileName) {
        memory.loadProgram(fileName);
    }

    private String getCommand() {
        return new String(memory.getCodeWord(processor.IC.getValue()));
    }

}
