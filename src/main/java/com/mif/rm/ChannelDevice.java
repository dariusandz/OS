package com.mif.rm;

import javafx.util.Pair;

public class ChannelDevice {
    public static Register SB, DB, ST, DT;

    public ChannelDevice() {
        initRegisters();
    }

    private void initRegisters() {
        SB = new Register();
        DB = new Register();
        ST = new Register();
        DT = new Register();
    }

    public String process(Pair<Integer, String> siValuePair) {
        if(siValuePair.getKey() == 1) {
            ST.setValue(4);
            DT.setValue(1);
        }
        if(siValuePair.getKey() == 2 || siValuePair.getKey() == 4) {
            ST.setValue(1);
            DT.setValue(4);
        }
        return siValuePair.getValue();
    }
}
