package com.mif.vm;

import com.mif.common.ByteUtil;
import com.mif.common.Register;

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
            IC.incrementValue();
        }
    }

    private boolean processSIValue() {
        switch (SI.getValue()) {
            case 1:
                int letterCount = Bx.getValue();
                byte[] address = Ax.getByteValue();
                String outputText = "";
                Scanner scan = new Scanner(System.in);
                while(outputText.length() != letterCount){
                    System.out.println("Type in " + letterCount + " symbols");
                    outputText = scan.nextLine();
                }
                byte[] bytes = outputText.getBytes(StandardCharsets.UTF_8);
                memory.putBytesToMemory(address[2], address[3], bytes, bytes.length);
                break;
            case 2:
                byte[] bytes1 = memory.getBytesFromMemory(Ax.getByteValue()[2], Ax.getByteValue()[3], Bx.getValue());
                System.out.println(new String(bytes1, StandardCharsets.UTF_8));
                break;
            case 3:
                return false;
            case 4:
                System.out.println(new String(memory.getWordFromMemory(Ax.getByteValue()[2], Ax.getByteValue()[3]), StandardCharsets.UTF_8));
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
                return true;
        }
        return true;
    }

    private String getCommand() { return new String(memory.getCodeWord(IC.getValue()));
    }

}
