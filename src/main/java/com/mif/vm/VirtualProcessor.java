package com.mif.vm;

import com.mif.common.Register;

public class VirtualProcessor extends Command {

    public VirtualProcessor(VirtualMemory memory) {
        super(memory);
    }

    public void runProgram() {
        String command;
        while(!(command = getCommand()).equals("HALT")) {
            processCommand(command);
            IC.incrementValue(4);
        }

    }

    private String getCommand() {
        return memory.getWord(IC.getValue());
    }

}
