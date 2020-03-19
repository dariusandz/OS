package com.mif.vm;

import com.mif.common.Register;
import com.mif.common.ByteUtil;

import java.nio.ByteBuffer;
import static com.mif.vm.CMD.*;

public class Command {

    private static final int pageSize = 16;

    IMemory memory;
    protected Register IC;
    Register Ax, Bx, SP, PR, SI, ZF;

    public Command(VirtualMemory memory) {
        this.memory = memory;
        this.IC = new Register(0);
        this.Ax = new Register(0);
        this.Bx = new Register(0);
        this.SP = new Register(0);
        this.PR = new Register(0);
        this.SI = new Register(0);
        this.ZF = new Register(0);
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
        else if (getCommandValue(command, 4) == JUMP.getValue()) {
            JUMP();
        }
        else if (getCommandValue(command, 4) == JMPG.getValue()) {
            JMPG();
        }
        else if (getCommandValue(command, 4) == JMPL.getValue()) {
            JMPL();
        }
        else if (getCommandValue(command, 4) == JMPZ.getValue()) {
            JMPZ();
        }
        else if (getCommandValue(command, 4) == JPNZ.getValue()) {
            JPNZ();
        }
        else if (getCommandValue(command, 4) == LOOP.getValue()) {
            LOOP();
        }
        else if (getCommandValue(command, 4) == HALT.getValue()) {
            HALT();
        }
        else if (getCommandValue(command, 4) == STSB.getValue()) {
            STSB(command);
        }
        else if (getCommandValue(command, 4) == LDSB.getValue()) {
            LDSB(command);
        }
        else if (getCommandValue(command, 4) == PUSH.getValue()) {
            PUSH();
        }
        else if (getCommandValue(command, 4) == POPP.getValue()) {
            POPP();
        }
        else if (getCommandValue(command, 4) == PRNT.getValue()) {
            PRNT();
        }
        else if (getCommandValue(command, 4) == PNUM.getValue()) {
            PNUM();
        }
        else if (getCommandValue(command, 4) == SCAN.getValue()) {
            SCAN();
        }
        else if (getCommandValue(command, 4) == LOAD.getValue()) {
            LOAD();
        }
        else if (getCommandValue(command, 4) == MONT.getValue()) {
            MONT();
        }
        else if (getCommandValue(command, 4) == UNMT.getValue()) {
            UNMT();
        }
        else if (getCommandValue(command, 4) == POWR.getValue()) {
            POWR();
        }
        else if (getCommandValue(command, 4) == SVAL.getValue()) {
            SVAL();
        }
        else if (getCommandValue(command, 4) == GVAL.getValue()) {
            GVAL();
        }
        else if (getCommandValue(command, 4) == TYPE.getValue()) {
            TYPE();
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
        IC.incrementValue();
        return new String(memory.getCodeWord(IC.getValue()));
    }

    private char getRegChar(String command, int regIndex) {
        return command.charAt(regIndex);
    }

    private void LDN(String command) { // i registra talpinamas skaicius
        char regChar = getRegChar(command, 3);

        String hexValue = nextWord();
        getRegister(regChar).setValue(
                ByteUtil.stringBytesToInt(hexValue)
        );
    }

    private void LDM(String command) { // i registra keliamas zodis is atminties
        char regChar = getRegChar(command, 3);

        String hexValue = nextWord();
        int page = ByteUtil.getIthByteFromString(hexValue, 2);
        int word = ByteUtil.getIthByteFromString(hexValue, 3);

        getRegister(regChar).setValue(memory.getWordFromMemory(page, word));
    }

    private void SVR(String command) { // i atminti talpinam registro reiksme
        char regChar = getRegChar(command, 3);
        int regVal = getRegister(regChar).getValue();

        String hexValue = nextWord();
        int page = ByteUtil.getIthByteFromString(hexValue, 2);
        int word = ByteUtil.getIthByteFromString(hexValue, 3);

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
            ZF.setValue(0);
        else ZF.setValue(1);
    }

    private void SB(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() - getRegister(r2).getValue()
        );
        if(getRegister(r1).getValue() == 0)
            ZF.setValue(0);
        else ZF.setValue(1);
    }

    private void ML(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() * getRegister(r2).getValue()
        );
        if(getRegister(r1).getValue() == 0)
            ZF.setValue(0);
        else ZF.setValue(1);
    }

    private void DV(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() / getRegister(r2).getValue()
        );
        if(getRegister(r1).getValue() == 0)
            ZF.setValue(0);
        else ZF.setValue(1);
    }

    private void CM(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        int diff = getRegister(r1).getValue() - getRegister(r2).getValue();
        if (diff == 0)
            PR.setValue(0);
        if (diff > 0)
            PR.setValue(1);
        if (diff < 0)
            PR.setValue(2);
        if(diff == 0)
            ZF.setValue(0);
        else ZF.setValue(1);
    }

    private void AN(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() & getRegister(r2).getValue()
        );
        if(getRegister(r1).getValue() == 0)
            ZF.setValue(0);
        else ZF.setValue(1);
    }

    private void XR(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() ^ getRegister(r2).getValue()
        );
        if(getRegister(r1).getValue() == 0)
            ZF.setValue(0);
        else ZF.setValue(1);
    }

    private void OR(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() ^ getRegister(r2).getValue()
        );
        if(getRegister(r1).getValue() == 0)
            ZF.setValue(0);
        else ZF.setValue(1);
    }

    private void NOT(String command) {
        char r1 = getRegChar(command, 3);

        getRegister(r1).setValue(
                ~getRegister(r1).getValue()
        );
        if(getRegister(r1).getValue() == 0)
            ZF.setValue(0);
        else ZF.setValue(1);
    }

    private void LS(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() << getRegister(r2).getValue()
        );
        if(getRegister(r1).getValue() == 0)
            ZF.setValue(0);
        else ZF.setValue(1);
    }

    private void RS(String command) {
        char r1 = getRegChar(command, 2);
        char r2 = getRegChar(command, 3);

        getRegister(r1).setValue(
                getRegister(r1).getValue() >> getRegister(r2).getValue()
        );
        if(getRegister(r1).getValue() == 0)
            ZF.setValue(0);
        else ZF.setValue(1);
    }

    private void JUMP() {
        String hexValue = nextWord();
        int page = ByteUtil.getIthByteFromString(hexValue, 2);
        int word = ByteUtil.getIthByteFromString(hexValue, 3);
        IC.setValue(page*pageSize+word);
    }

    private void JMPG() {
        if(PR.getValue() == 1) {
            JUMP();
        }
        else nextWord();
    }

    private void JMPL() {
        if(PR.getValue() == 2) {
            JUMP();
        }
        else nextWord();
    }

    private void JMPZ() {
        if(ZF.getValue() == 0) {
            JUMP();
        }
        else nextWord();
    }

    private void JPNZ() {
        if(ZF.getValue() != 0) {
            JUMP();
        }
        else nextWord();
    }

    private void LOOP() {
        Ax.setValue(
                Ax.getValue() - 1
        );

        if (Ax.getValue() != 0) {
            JUMP();
        }
        else nextWord();
    }

    private void HALT() {
        SI.setValue(3);
    }

    private void STSB(String command) {
        // TODO
    }

    private void LDSB(String command) {
        // TODO
    }

    private void PUSH() {
        memory.putWordToMemory(SP.getValue()/16,SP.getValue()%16, Ax.getByteValue());
        SP.incrementValue(4);
    }

    private void POPP() {
        Ax.setValue(memory.getWordFromMemory(SP.getValue()/16,SP.getValue()%16));
        SP.incrementValue(-4);
    }

    private void PRNT() {
        SI.setValue(2);
    }

    private void PNUM() {
        SI.setValue(4);
    }

    private void SCAN() {
        SI.setValue(1);
    }

    private void LOAD() {
        SI.setValue(5);
    }

    private void MONT() {
        SI.setValue(10);
    }

    private void UNMT() {
        SI.setValue(11);
    }

    private void POWR() {
        SI.setValue(6);
    }

    private void SVAL() {
        SI.setValue(7);
    }

    private void GVAL() {
        SI.setValue(8);
    }

    private void TYPE() {
        SI.setValue(9);
    }

}
