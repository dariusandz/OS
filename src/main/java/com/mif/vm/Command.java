package com.mif.vm;

import com.mif.common.Register;
import com.mif.common.Util;

import java.nio.ByteBuffer;
import static com.mif.vm.CMD.*;

public class Command {

    protected IMemory memory;
    protected Register IC;
    private Register Ax, Bx;

    public Command(VirtualMemory memory) {
        this.memory = memory;
        this.IC = new Register(0);
        this.Ax = new Register(0);
        this.Bx = new Register(0);
    }

    protected void processCommand(String command) {
        if (getCommandValue(command, 3) == LDN.getValue()) {
            LDN(command);
        }
        else if (getCommandValue(command, 3) == LDM.getValue()) {
            LDM(command);
        }
        else if (getCommandValue(command, 2) == SV.getValue()) {
            System.out.println("SV");
        }
        else if (getCommandValue(command, 2) == CP.getValue()) {
            System.out.println("CP");
        }
        else if (getCommandValue(command, 2) == AD.getValue()) {
            System.out.println("AD");
        }
        else if (getCommandValue(command, 2) == SB.getValue()) {
            System.out.println("SB");
        }
        else if (getCommandValue(command, 2) == ML.getValue()) {
            System.out.println("ML");
        }
        else if (getCommandValue(command, 2) == DV.getValue()) {
            System.out.println("DV");
        }
        else if (getCommandValue(command, 2) == CM.getValue()) {
            System.out.println("CM");
        }
        else if (getCommandValue(command, 2) == AN.getValue()) {
            System.out.println("AN");
        }
    }

    private int getCommandValue(String command, int commandLen) {
        if (commandLen == 2)
            return (ByteBuffer.wrap(command.getBytes()).getInt() & 0xffff0000);
        else if (commandLen == 3)
            return (ByteBuffer.wrap(command.getBytes()).getInt() & 0xffffff00);
        else
            return (ByteBuffer.wrap(command.getBytes()).getInt() & 0xffffffff);
    }

    private Register getRegister(char regChar) {
        return regChar == 'A' ? Ax : Bx;
    }

    private String nextWord() {
        IC.incrementValue(4);
        return memory.getWord(IC.getValue());
    }

    private void LDN(String command) {
        char regChar = command.charAt(3);
        getRegister(regChar).setValue(
                Util.stringBytesToInt(nextWord())
        );
    }

    private void LDM(String command) {
        char register = command.charAt(3);

    }

}
