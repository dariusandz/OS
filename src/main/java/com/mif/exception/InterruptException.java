package com.mif.exception;

public class InterruptException extends RuntimeException {

    private String message;
    private String interrupt;

    public InterruptException(String message, String interrupt) {
        this.message = message;
        this.interrupt = interrupt;
    }

    public String getLocalCause() {
        return message + " " + interrupt;
    }
}
