package com.mif.exception;

public class OutOfMemoryException extends RuntimeException {

    private String message;

    public OutOfMemoryException(String message) {
        this.message = "Klaida: " + message;
    }

    public String getMessage() {
        return this.message;
    }
}
