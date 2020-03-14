package com.mif.vm;

import java.nio.ByteBuffer;

public enum CMD {

    LDN("LDN"), LDM("LDM"), SVR("SVR"), CP("CP"), // Atminties ir registru valdymo
    AD("AD"), SB("SB"), ML("ML"), DV("DV"), CM("CM"), AN("AN"), // Aritmetines
    XR("XR"), OR("OR"), NOT("NOT"), LS("LS"), RS("RS"), // Loginiu operaciju
    JUMP("JUMP"), JMPG("JMPG"), JMPL("JMPL"), JMPZ("JMPZ"), JPNZ("JPNZ"), LOOP("LOOP"), HALT("HALT"), STSB("STSB"), LDSB("LDSB"), // Valdymo perdavimo
    PUSH("PUSH"), POPP("POPP"), // Darbo su steku
    PRNT("PRNT"), PNUM("PNUM"), SCAN("SCAN"), LOAD("LOAD"), // Darbo su isvedimo ivedimo irenginiais
    MONT("MONT"), UMNT("UMNT"), POWR("POWR"), SVAL("SVAL"), GVAL("GVAL"), TYPE("TYPE"); // Prietaisu instrukcijos

    private int value;

    CMD(String name) {
        this.value = getIntValue(name);
    }

    public int getValue() { return this.value; }

    private int getIntValue(String command) {
        StringBuilder sb = new StringBuilder(command);
        for (int i = command.length(); i < 4; i++)
            sb.append(" ");

        if (command.length() == 2)
            return (ByteBuffer.wrap(sb.toString().getBytes()).getInt() & 0xffff0000);
        else if (command.length() == 3)
            return (ByteBuffer.wrap(sb.toString().getBytes()).getInt() & 0xffffff00);
        else
            return (ByteBuffer.wrap(sb.toString().getBytes()).getInt() & 0xffffffff);
    }

}