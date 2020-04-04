package com.mif.exception;

public class HarmlessInterruptException extends InterruptException {

    public HarmlessInterruptException(String message, String interrupt) {
        super(message, interrupt);
    }
}
