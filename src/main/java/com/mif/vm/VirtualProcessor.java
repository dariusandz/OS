package com.mif.vm;

import com.mif.common.Register;

public class VirtualProcessor extends Command {

    private IMemory memory;
    private Register IC;

    public boolean running = false;

    public VirtualProcessor(VirtualMemory memory) {
        this.IC = new Register(0);
        this.memory = memory;
    }

    public void runProgram() {
        String command;
        while(!(command = getCommand()).equals("HALT")) {
            IC.incrementValue(wordSize);
        }
    }

    private String getCommand() {
        return memory.getCommand(IC.getValue());
    }

}
