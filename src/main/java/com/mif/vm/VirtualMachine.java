package com.mif.vm;

import com.mif.common.FileReader;
import com.mif.common.IdGenerator;
import com.mif.rm.Processor;

import java.util.ArrayList;
import java.util.List;


public class VirtualMachine {

    private static FileReader reader = new FileReader();

    private static IdGenerator idGenerator = new IdGenerator();

    private Long id;

    public VirtualProcessor virtualProcessor;
    public VirtualMemory virtualMemory;

    private String programName;

    public VirtualMachine(String programName, List<String> params) {
        this.virtualMemory = new VirtualMemory(params);
        this.virtualProcessor = new VirtualProcessor(virtualMemory);
        this.programName = programName;
        this.id = idGenerator.getVmId();
        Processor.TI.setValue(30);
        Processor.DI.setValue(0);
        Processor.DS.setValue(0);
    }

    public Long getId() {
        return id;
    }

    public String getCommand() {
        return virtualProcessor.getCommand();
    }

    public String processCommand() {
        return virtualProcessor.processCommand(getCommand());
    }

    public List<String> loadProgram() {
        String program = reader.loadProgramAsString(this.programName);
        if(!virtualProcessor.loadInstructionsFromFile(programName))
            return null;
        String[] programArrays = program.split("@dataseg",2)[1].split("@codeseg",3);
        putDataSeg(programArrays[0]);
        return getCommands(programArrays[1].replace(" ", ""));
    }

    public void putDataSeg(String dataSeg) {
        virtualMemory.putDataSegIntoMemory(dataSeg);
    }

    public void freeMemory() {
        virtualMemory.freeMemory();
    }

    private List<String> getCommands(String program) {
        List<String> commands = new ArrayList<>();
        StringBuilder command = new StringBuilder();
        for (int i = 0; i < program.length(); i += 4) {
            String cmd = program.substring(i, i + 4);
            command.append(cmd);
            if (cmd.contains("LD") || cmd.contains("SVR") || cmd.startsWith("J")) {
                command.append(program.substring(i + 4, i + 12));
                i += 8;
            }
            commands.add(command.toString());
            command.setLength(0);
        }
        return commands;
    }
}
