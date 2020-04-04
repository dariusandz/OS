package com.mif.exception;

public class FatalInterruptException extends InterruptException {

    public FatalInterruptException(String message, String interrupt) {
        super(message, interrupt);
    }
}
