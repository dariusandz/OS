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
        else if (getCommandValue(command, 3) == SVR.getValue()) {
            SVR(command);
        }
        else if (getCommandValue(command, 2) == CP.getValue()) {
            CP(command);
        }
        else if (getCommandValue(command, 2) == AD.getValue()) {
            AD(command);
        }
        else if (getCommandValue(command, 2) == SB.getValue()) {
            SB(command);
        }
        else if (getCommandValue(command, 2) == ML.getValue()) {
            ML(command);
        }
        else if (getCommandValue(command, 2) == DV.getValue()) {
            DV(command);
        }
        else if (getCommandValue(command, 2) == CM.getValue()) {
            CM(command);
        }
        else if (getCommandValue(command, 2) == AN.getValue()) {
            AN(command);
        }
        else if (getCommandValue(command, 2) == XR.getValue()) {
            XR(command);
        }
        else if (getCommandValue(command, 2) == OR.getValue()) {
            OR(command);
        }
        else if (getCommandValue(command, 3) == NOT.getValue()) {
            NOT(command);
        }
        else if (getCommandValue(command, 2) == LS.getValue()) {
            LS(command);
        }
        else if (getCommandValue(command, 2) == RS.getValue()) {
            RS(command);
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

    private char getRegChar(String command, int regIndex) {
        return command.charAt(regIndex);
    }

    private void LDN(String command) {
        char regChar = getRegChar(command, 3);

        String hexValue = nextWord();
        getRegister(regChar).setValue(
                Util.stringBytesToInt(hexValue)
        );
    }

    private void LDM(String command) {
        char regChar = getRegChar(command, 3);

        String hexValue = nextWord();
        int page = Util.getIthByteFromString(hexValue, 2);
        int word = Util.getIthByteFromString(hexValue, 3);

        memory.getWordFromMemory(page, word);
    }

    private void SVR(String command) {
        char regChar = getRegChar(command, 3);
        int regVal = getRegister(regChar).getValue();

        String hexValue = nextWord();
        int page = Util.getIthByteFromString(hexValue, 2);
        int word = Util.getIthByteFromString(hexValue, 3);

        memory.putValueToMemory(regVal, page, word);
    }

    private void CP(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r2).getValue()
        );
    }

    private void AD(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r2).getValue() + getRegister(r1).getValue()
        );
    }

    private void SB(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() - getRegister(r2).getValue()
        );
    }

    private void ML(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() * getRegister(r2).getValue()
        );
    }

    private void DV(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() / getRegister(r2).getValue()
        );
    }

    private void CM(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        int diff = getRegister(r1).getValue() - getRegister(r2).getValue();
        if (diff == 0)
            ; // TODO set PR register value depending on above result
    }

    private void AN(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() & getRegister(r2).getValue()
        );
    }

    private void XR(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() ^ getRegister(r2).getValue()
        );
    }

    private void OR(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() ^ getRegister(r2).getValue()
        );
    }

    private void NOT(String command) {
        char r1 = getRegChar(command, 3);

        getRegister(r1).setValue(
                ~getRegister(r1).getValue()
        );
    }

    private void LS(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() << getRegister(r2).getValue()
        );
    }

    private void RS(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() >> getRegister(r2).getValue()
        );
    }


}
