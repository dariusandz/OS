package com.mif.rm;

import com.mif.common.Register;

public class Processor {

    public ProcessorMode processorMode;

    public Register PTR, AX, BX;
    public Register IC, PI, SI, TI, PR, SP, MODE, ES, DI;

    public Processor() { this.processorMode = ProcessorMode.SUPERVISOR; }

    public Processor(ProcessorMode mode) { this.processorMode = mode; }
}
