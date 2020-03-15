package com.mif.vm;

import com.mif.common.Register;
import com.mif.common.ByteUtil;
import com.mif.rm.Processor;

import java.nio.ByteBuffer;
import static com.mif.vm.CMD.*;

public class Command {

    protected VirtualMemory memory;
    protected Processor processor;

    public Command(VirtualMemory memory) {
        this.memory = memory;
        this.processor = Processor.getInstance();
    }

    protected void processCommand(String command) {
        if (getCommandByteValue(command, 3) == LDN.getValue()) {
            LDN(command);
        }
        else if (getCommandByteValue(command, 3) == LDM.getValue()) {
            LDM(command);
        }
        else if (getCommandByteValue(command, 3) == SVR.getValue()) {
            SVR(command);
        }
        else if (getCommandByteValue(command, 2) == CP.getValue()) {
            CP(command);
        }
        else if (getCommandByteValue(command, 2) == AD.getValue()) {
            AD(command);
        }
        else if (getCommandByteValue(command, 2) == SB.getValue()) {
            SB(command);
        }
        else if (getCommandByteValue(command, 2) == ML.getValue()) {
            ML(command);
        }
        else if (getCommandByteValue(command, 2) == DV.getValue()) {
            DV(command);
        }
        else if (getCommandByteValue(command, 2) == CM.getValue()) {
            CM(command);
        }
        else if (getCommandByteValue(command, 2) == AN.getValue()) {
            AN(command);
        }
        else if (getCommandByteValue(command, 2) == XR.getValue()) {
            XR(command);
        }
        else if (getCommandByteValue(command, 2) == OR.getValue()) {
            OR(command);
        }
        else if (getCommandByteValue(command, 3) == NOT.getValue()) {
            NOT(command);
        }
        else if (getCommandByteValue(command, 2) == LS.getValue()) {
            LS(command);
        }
        else if (getCommandByteValue(command, 2) == RS.getValue()) {
            RS(command);
        }
        else if (getCommandByteValue(command, 4) == JUMP.getValue()) {
            JUMP(command);
        }
        else if (getCommandByteValue(command, 4) == JMPG.getValue()) {
            JMPG(command);
        }
        else if (getCommandByteValue(command, 4) == JMPL.getValue()) {
            JMPL(command);
        }
        else if (getCommandByteValue(command, 4) == JMPZ.getValue()) {
            JMPZ(command);
        }
        else if (getCommandByteValue(command, 4) == JPNZ.getValue()) {
            JPNZ(command);
        }
        else if (getCommandByteValue(command, 4) == LOOP.getValue()) {
            LOOP(command);
        }
        else if (getCommandByteValue(command, 4) == HALT.getValue()) {
            HALT(command);
        }
        else if (getCommandByteValue(command, 4) == STSB.getValue()) {
            STSB(command);
        }
        else if (getCommandByteValue(command, 4) == LDSB.getValue()) {
            LDSB(command);
        }
        else if (getCommandByteValue(command, 4) == PUSH.getValue()) {
            PUSH(command);
        }
        else if (getCommandByteValue(command, 4) == POPP.getValue()) {
            POPP(command);
        }
        else if (getCommandByteValue(command, 4) == PRNT.getValue()) {
            PRNT(command);
        }
        else if (getCommandByteValue(command, 4) == PNUM.getValue()) {
            PNUM(command);
        }
        else if (getCommandByteValue(command, 4) == SCAN.getValue()) {
            SCAN(command);
        }
        else if (getCommandByteValue(command, 4) == LOAD.getValue()) {
            LOAD(command);
        }
        else if (getCommandByteValue(command, 4) == MONT.getValue()) {
            MONT(command);
        }
        else if (getCommandByteValue(command, 4) == UMNT.getValue()) {
            UMNT(command);
        }
        else if (getCommandByteValue(command, 4) == POWR.getValue()) {
            POWR(command);
        }
        else if (getCommandByteValue(command, 4) == SVAL.getValue()) {
            SVAL(command);
        }
        else if (getCommandByteValue(command, 4) == GVAL.getValue()) {
            GVAL(command);
        }
        else if (getCommandByteValue(command, 4) == TYPE.getValue()) {
            TYPE(command);
        }

        processor.IC.incrementValue();
    }

    private int getCommandByteValue(String command, int commandLen) {
        if (commandLen == 2)
            return (ByteBuffer.wrap(command.getBytes()).getInt() & 0xffff0000);
        else if (commandLen == 3)
            return (ByteBuffer.wrap(command.getBytes()).getInt() & 0xffffff00);
        else
            return (ByteBuffer.wrap(command.getBytes()).getInt() & 0xffffffff);
    }

    private Register getRegister(char regChar) {
        return regChar == 'A' ? processor.AX : processor.BX;
    }

    private String nextWord() {
        processor.IC.incrementValue();
        return new String(memory.getCodeWord(processor.IC.getValue()));
    }

    private char getRegChar(String command, int regIndex) {
        return command.charAt(regIndex);
    }

    private void LDN(String command) {
        char regChar = getRegChar(command, 3);

        String hexValue = nextWord();
        getRegister(regChar).setValue(
                ByteUtil.stringHexToInt(hexValue)
        );
    }

    private void LDM(String command) {
        char regChar = getRegChar(command, 3);

        String hexValue = nextWord();
        int page = ByteUtil.getIthByteFromString(hexValue, 2);
        int word = ByteUtil.getIthByteFromString(hexValue, 3);

        memory.getWordFromMemory(page, word);
    }

    private void SVR(String command) {
        char regChar = getRegChar(command, 3);
        int regVal = getRegister(regChar).getValue();

        String hexValue = nextWord();
        int page = ByteUtil.getIthByteFromString(hexValue, 2);
        int word = ByteUtil.getIthByteFromString(hexValue, 3);

        memory.putValueToMemory(page, word, regVal);
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
            processor.PR.setValue(0);
        else if (diff < 0)
            processor.PR.setValue(2);
        else if(diff > 0)
            processor.PR.setValue(1);
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

    private void JUMP(String command) {
        String hexVal = nextWord();
        processor.IC.setValue(
                ByteUtil.stringHexToInt(hexVal)
        );
    }

    private void JMPG(String command) {
        String hexVal = nextWord();
        if (processor.PR.getValue() == 0) {
            processor.IC.setValue(
                    ByteUtil.stringHexToInt(hexVal)
            );
        }
    }

    private void JMPL(String command) {
        String hexVal = nextWord();
        if (processor.PR.getValue() == 1) {
            processor.IC.setValue(
                    ByteUtil.stringHexToInt(hexVal)
            );
        }
    }

    private void JMPZ(String command) {
        String hexVal = nextWord();
        if (processor.PR.getValue() == 2) {
            processor.IC.setValue(
                    ByteUtil.stringHexToInt(hexVal)
            );
        }
    }

    private void JPNZ(String command) {
        String hexVal = nextWord();
        if (processor.PR.getValue() == 3) {
            processor.IC.setValue(
                    ByteUtil.stringHexToInt(hexVal)
            );
        }
    }

    private void LOOP(String command) {
        processor.AX.setValue(
                processor.AX.getValue() - 1
        );

        if (processor.AX.getValue() != 0) {
            String hexVal = nextWord();
            processor.IC.setValue(
                    ByteUtil.stringHexToInt(hexVal)
            );
        }
    }

    private void HALT(String command) {
        processor.SI.setValue(
                3
        );
    }

    private void STSB(String command) {

    }

    private void LDSB(String command) {

    }

    private void PUSH(String command) {

    }

    private void POPP(String command) {

    }

    private void PRNT(String command) {
        processor.SI.setValue(
                2
        );
    }

    private void PNUM(String command) {
        processor.SI.setValue(
                4
        );
    }

    private void SCAN(String command) {
        processor.SI.setValue(
                1
        );
    }

    private void LOAD(String command) {
        processor.SI.setValue(
                5
        );
    }

    private void MONT(String command) {

    }

    private void UMNT(String command) {

    }

    private void POWR(String command) {
        processor.SI.setValue(
                6
        );
    }

    private void SVAL(String command) {
        processor.SI.setValue(
                7
        );
    }

    private void GVAL(String command) {
        processor.SI.setValue(
                8
        );
    }

    private void TYPE(String command) {
        processor.SI.setValue(
                9
        );
    }

}
