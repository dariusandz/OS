package com.mif.common;

public class IdGenerator {

    private Long id = Long.valueOf(0);

    public Long getId() {
        return id++;
    }
}
