package com.mif.rm;

public class RealMachine {
    Processor processor;
    public RealMachine() {
        this.processor = new Processor();
        processor.run();
    }
}
