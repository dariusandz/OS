package com.mif.vm;



import java.nio.charset.StandardCharsets;
import java.util.Scanner;

public class VirtualProcessor extends Command {

    public VirtualProcessor(VirtualMemory memory) {
        super(memory);
    }

    public void runProgram() {
        while (processSIValue()) {
            String command = getCommand();
            processCommand(command);
        }
    }

    private boolean processSIValue() {
        switch (processor.SI.getValue()) {
            case 1:
                int letterCount = processor.BX.getValue();
                byte[] address = processor.AX.getByteValue();
                String outputText = "";
                Scanner scan = new Scanner(System.in);
                while (outputText.length() != letterCount) {
                    System.out.println("Type in " + letterCount + " symbols");
                    outputText = scan.nextLine();
                }
                byte[] bytes = outputText.getBytes(StandardCharsets.UTF_8);
                memory.putBytesToMemory(address[2], address[3], bytes, bytes.length);
                break;
            case 2:
                byte[] bytes1 = memory.getBytesFromMemory(processor.AX.getByteValue()[2], processor.AX.getByteValue()[3], processor.BX.getValue());
                System.out.println(new String(bytes1, StandardCharsets.UTF_8));
                break;
            case 3:
                return false;
            case 4:
                System.out.println(new String(memory.getWordFromMemory(processor.AX.getByteValue()[2], processor.AX.getByteValue()[3]), StandardCharsets.UTF_8));
                break;
            case 5:
                // TODO LOAD command
                break;
            case 6:
                // TODO device controller
                break;
            case 7:
                break;
            case 8:
                break;
            case 9:
                break;
            default:
                processor.SI.setValue(0);
                return true;
        }
        return true;
    }

    public void loadInstructionsFromFile(String fileName) {
        memory.loadProgram(fileName);
    }

    private String getCommand() {
        return new String(memory.getCodeWord(processor.IC.getValue()));
    }

}
