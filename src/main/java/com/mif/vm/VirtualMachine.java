package com.mif.vm;

import javafx.application.Application;
import javafx.stage.Stage;

public class VirtualMachine extends Application {

    private static int vmCount;

    private VirtualProcessor virtualProcessor;
    public VirtualMemory virtualMemory;

    public VirtualMachine() {
        this.vmCount++;
        this.virtualMemory = new VirtualMemory();
        this.virtualProcessor = new VirtualProcessor(virtualMemory);
    }

    public void run() {
        virtualProcessor.runProgram();
    }

    public void loadProgram(String filename) {
        virtualProcessor.loadInstructionsFromFile(filename);
    }

    public void freeMemory() {
        virtualMemory.freeMemory();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {
        primaryStage.setTitle("Virtual mašina nr. " + vmCount);
        primaryStage.show();
    }
}
