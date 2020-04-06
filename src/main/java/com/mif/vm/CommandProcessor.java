package com.mif.vm;

import com.mif.rm.Memory;
import com.mif.rm.Register;
import com.mif.common.ByteUtil;
import com.mif.rm.Processor;

import java.nio.ByteBuffer;
import static com.mif.vm.CMD.*;
import static com.mif.vm.VirtualMemory.CODESEG_START_PAGE;
import static com.mif.vm.VirtualMemory.EXTRASEG_START_PAGE;
import static com.mif.vm.VirtualMemory.PARAMSEG_START_PAGE;

public class CommandProcessor {

    public VirtualMemory memory;
    protected Processor processor;

    public CommandProcessor(VirtualMemory memory) {
        this.memory = memory;
        this.processor = Processor.getInstance();
    }

    protected String processCommand(String command) {
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
            JUMP();
        }
        else if (getCommandByteValue(command, 4) == JMPG.getValue()) {
            JMPG();
        }
        else if (getCommandByteValue(command, 4) == JMPL.getValue()) {
            JMPL();
        }
        else if (getCommandByteValue(command, 4) == JMPZ.getValue()) {
            JMPZ();
        }
        else if (getCommandByteValue(command, 4) == JPNZ.getValue()) {
            JPNZ();
        }
        else if (getCommandByteValue(command, 4) == LOOP.getValue()) {
            LOOP();
        }
        else if (getCommandByteValue(command, 4) == HALT.getValue()) {
            HALT(command);
        }
        else if (getCommandByteValue(command, 4) == STSB.getValue()) {
            STSB();
        }
        else if (getCommandByteValue(command, 4) == LDSB.getValue()) {
            LDSB();
        }
        else if (getCommandByteValue(command, 4) == PUSH.getValue()) {
            PUSH();
        }
        else if (getCommandByteValue(command, 4) == POPP.getValue()) {
            POPP();
        }
        else if (getCommandByteValue(command, 4) == PRNT.getValue()) {
            PRNT();
        }
        else if (getCommandByteValue(command, 4) == PNUM.getValue()) {
            PNUM();
        }
        else if (getCommandByteValue(command, 4) == SCAN.getValue()) {
            SCAN();
        }
        else if (getCommandByteValue(command, 4) == LOAD.getValue()) {
            LOAD();
        }
        else if (getCommandByteValue(command, 4) == MONT.getValue()) {
            MONT();
        }
        else if (getCommandByteValue(command, 4) == UNMT.getValue()) {
            UNMT();
        }
        else if (getCommandByteValue(command, 4) == POWR.getValue()) {
            POWR();
        }
        else if (getCommandByteValue(command, 4) == SVAL.getValue()) {
            SVAL();
        }
        else if (getCommandByteValue(command, 4) == GVAL.getValue()) {
            GVAL();
        }
        else if (getCommandByteValue(command, 4) == TYPE.getValue()) {
            TYPE();
        }
        else {
            Processor.PI.setValue(2);
            return "";
        }

        Processor.IC.incrementValue();
        if(getCommandByteValue(command, 4) == PRNT.getValue() ||
                getCommandByteValue(command, 4) == SCAN.getValue())
            Processor.TI.setValue(Processor.TI.getValue() - 3);
        else
            Processor.TI.setValue(Processor.TI.getValue() - 1);
        return command;
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

    private void LDN(String command) { // i registra talpinamas skaicius
        char regChar = getRegChar(command, 3);

        String hexValue = nextWord();
        getRegister(regChar).setValue(
                ByteUtil.stringHexToInt(hexValue)
        );
    }

    private void LDM(String command) { // i registra keliamas zodis is atminties
        char regChar = getRegChar(command, 3);

        String hexValue = nextWord();
        int page = ByteUtil.getIthByteFromString(hexValue, 2);
        int word = ByteUtil.getIthByteFromString(hexValue, 3);

        if(page > CODESEG_START_PAGE) {
            Processor.PI.setValue(2);
            return;
        }
        getRegister(regChar).setValue(memory.getWordFromMemory(page, word));
    }

    private void SVR(String command) { // i atminti talpinam registro reiksme
        char regChar = getRegChar(command, 3);
        int regVal = getRegister(regChar).getValue();

        String hexValue = nextWord();
        int page = ByteUtil.getIthByteFromString(hexValue, 2);
        int word = ByteUtil.getIthByteFromString(hexValue, 3);

        if(page > CODESEG_START_PAGE) {
            Processor.PI.setValue(2);
            return;
        }
        memory.putValueToMemory(page, word, regVal);
    }

    private void CP(String command) { // kopijuoja 2 registro reikmse i pirma
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
        if(getRegister(r1).getValue() == 0)
            processor.ZF.setValue(1);
        else processor.ZF.setValue(0);
    }

    private void SB(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() - getRegister(r2).getValue()
        );
        if(getRegister(r1).getValue() == 0)
            processor.ZF.setValue(1);
        else processor.ZF.setValue(0);
    }

    private void ML(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() * getRegister(r2).getValue()
        );
        if(getRegister(r1).getValue() == 0)
            processor.ZF.setValue(1);
        else processor.ZF.setValue(0);
    }

    private void DV(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() / getRegister(r2).getValue()
        );
        if(getRegister(r1).getValue() == 0)
            processor.ZF.setValue(1);
        else processor.ZF.setValue(0);
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
        if(diff == 0)
            processor.ZF.setValue(0);
        else processor.ZF.setValue(1);
    }

    private void AN(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() & getRegister(r2).getValue()
        );
        if(getRegister(r1).getValue() == 0)
            processor.ZF.setValue(1);
        else processor.ZF.setValue(0);
    }

    private void XR(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() ^ getRegister(r2).getValue()
        );
        if(getRegister(r1).getValue() == 0)
            processor.ZF.setValue(1);
        else processor.ZF.setValue(0);
    }

    private void OR(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() ^ getRegister(r2).getValue()
        );
        if(getRegister(r1).getValue() != 0)
            processor.ZF.setValue(0);
        else processor.ZF.setValue(1);
    }

    private void NOT(String command) {
        char r1 = getRegChar(command, 3);

        getRegister(r1).setValue(
                ~getRegister(r1).getValue()
        );
        if(getRegister(r1).getValue() != 0)
            processor.ZF.setValue(0);
        else processor.ZF.setValue(1);
    }

    private void LS(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() << getRegister(r2).getValue()
        );
        if(getRegister(r1).getValue() != 0)
            processor.ZF.setValue(0);
        else processor.ZF.setValue(1);
    }

    private void RS(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() >> getRegister(r2).getValue()
        );
        if(getRegister(r1).getValue() == 0)
            processor.ZF.setValue(0);
        else processor.ZF.setValue(1);
    }

    private void JUMP() {
        String hexVal = nextWord();
        processor.IC.setValue(
                ByteUtil.stringHexToInt(hexVal)
        );
    }

    private void JMPG() {
        String hexVal = nextWord();
        if (processor.PR.getValue() == 0) {
            processor.IC.setValue(
                    ByteUtil.stringHexToInt(hexVal)
            );
        }
    }

    private void JMPL() {
        String hexVal = nextWord();
        if (processor.PR.getValue() == 1) {
            processor.IC.setValue(
                    ByteUtil.stringHexToInt(hexVal)
            );
        }
    }

    private void JMPZ() {
        String hexVal = nextWord();
        if (processor.ZF.getValue() == 1) {
            processor.IC.setValue(
                    ByteUtil.stringHexToInt(hexVal)
            );
        }
    }

    private void JPNZ() {
        String hexVal = nextWord();
        if (processor.ZF.getValue() != 0) {
            processor.IC.setValue(
                    ByteUtil.stringHexToInt(hexVal)
            );
        }
    }

    private void LOOP() {
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

    private void STSB() {
        memory.putWordToMemory(VirtualMemory.EXTRASEG_START_PAGE, Processor.DI.getByteValue()[3], Processor.AX.getByteValue());
        Processor.DI.incrementValue();
    }

    private void LDSB() {
        Processor.AX.setValue(memory.getWordFromMemory(VirtualMemory.DATASEG_START_PAGE, Processor.DS.getValue()));
        Processor.DS.incrementValue();
    }

    private void PUSH() {
        memory.putWordToMemory(Processor.SP.getByteValue()[2], Processor.SP.getByteValue()[3], Processor.AX.getByteValue());
        Processor.SP.incrementValue(- 1);
    }

    private void POPP() {
        Processor.SP.incrementValue();
        Processor.AX.setValue(memory.getWordFromMemory(Processor.SP.getByteValue()[2], Processor.SP.getByteValue()[3]));
        memory.putWordToMemory(Processor.SP.getByteValue()[2], Processor.SP.getByteValue()[3], new byte[] {0,0,0,0});
    }

    private void PRNT() {
        if(Processor.AX.getByteValue()[2] > PARAMSEG_START_PAGE && Processor.AX.getByteValue()[2] < EXTRASEG_START_PAGE)
        processor.SI.setValue(
                2
        );
        else Processor.PI.setValue(1);
    }

    private void PNUM() {
        if(Processor.AX.getByteValue()[2] > PARAMSEG_START_PAGE && Processor.AX.getByteValue()[2] < EXTRASEG_START_PAGE)
        processor.SI.setValue(
                4
        );
        else Processor.PI.setValue(1);
    }

    private void SCAN() {
        if(Processor.AX.getByteValue()[2] > PARAMSEG_START_PAGE && Processor.AX.getByteValue()[2] < EXTRASEG_START_PAGE)
            Processor.SI.setValue(
                    1
            );
        else Processor.PI.setValue(1);
    }

    private void LOAD() {
        if(Processor.AX.getByteValue()[2] > PARAMSEG_START_PAGE && Processor.AX.getByteValue()[2] < EXTRASEG_START_PAGE)
            Processor.SI.setValue(
                    5
            );
        else Processor.PI.setValue(1);
    }

    private void MONT() {
        Processor.SI.setValue(10);
    }

    private void UNMT() {
        Processor.SI.setValue(11);
    }

    private void POWR() {
        processor.SI.setValue(
                6
        );
    }

    private void SVAL() {
        processor.SI.setValue(
                7
        );
    }

    private void GVAL() {
        processor.SI.setValue(
                8
        );
    }

    private void TYPE() {
        processor.SI.setValue(
                9
        );
    }

}
