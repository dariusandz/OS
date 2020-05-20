package com.mif.rm;

import javafx.util.Pair;

public class ChannelDevice {
    public static Register SB, DB, ST, DT;

    public ChannelDevice() {
        initRegisters();
    }

    public static void resetRegisters() {
        SB.setValue(0);
        DB.setValue(0);
        ST.setValue(0);
        DT.setValue(0);
    }

    private void initRegisters() {
        SB = new Register();
        DB = new Register();
        ST = new Register();
        DT = new Register();
    }

    void processSIValue(Pair<Integer, String> siValuePair) {
        if(siValuePair.getKey() == 1) {
            SB.setValue(1);
            DB.setValue(2);
            ST.setValue(4);
            DT.setValue(1);
        }
        if(siValuePair.getKey() == 2 || siValuePair.getKey() == 4) {
            SB.setValue(2);
            DB.setValue(1);
            ST.setValue(1);
            DT.setValue(4);
        }
    }
}
