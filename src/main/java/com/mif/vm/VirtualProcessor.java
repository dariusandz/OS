package com.mif.vm;

public class VirtualProcessor extends Command {

    public VirtualProcessor(VirtualMemory memory) {
        super(memory);

    }

    public void runProgram() {
        String command;
        while(!(command = getCommand()).equals("HALT")) {
            processCommand(command);
        }
    }

    public void loadInstructionsFromFile(String fileName) {
        memory.loadProgram(fileName);
    }

    private String getCommand() {
        return new String(memory.getCodeWord(processor.IC.getValue()));
    }

}
