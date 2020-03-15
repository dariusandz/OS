package com.mif.rm;

import com.mif.common.Register;

public class Processor {

    private static Processor processor = null;

    public ProcessorMode processorMode;

    public Register PTR, AX, BX;
    public Register IC, PI, SI, TI, PR, SP, MODE, ES, DI;

    public static Processor getInstance() {
        if (processor == null)
            return new Processor();

        return processor;
    }

    private Processor() {
        this.processorMode = ProcessorMode.SUPERVISOR;
        initializeRegisters();
    }

    private Processor(ProcessorMode mode) { this.processorMode = mode; }

    private void initializeRegisters() {
        PTR = new Register();
        AX = new Register();
        BX = new Register();
        IC = new Register();
        PI = new Register();
        SI = new Register();
        TI = new Register();
        PR = new Register();
        SP = new Register();
        MODE = new Register();
        ES = new Register();
        DI = new Register();
    }
}
