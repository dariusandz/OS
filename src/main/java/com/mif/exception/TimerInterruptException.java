package com.mif.exception;

public class TimerInterruptException extends RuntimeException{
    private String message;
    private String interrupt;

    public TimerInterruptException(String message, String interrupt) {
        this.message = message;
        this.interrupt = interrupt;
    }

    public String getLocalCause() {
        return message + " " + interrupt;
    }
}
