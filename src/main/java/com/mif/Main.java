package com.mif;

import com.mif.vm.VirtualMachine;

public class Main {

    public static void main(String[] args) {
        VirtualMachine vm = new VirtualMachine();
        vm.loadInstructionsFromFile("/pr1.txt");
        vm.run();
    }
}
