package com.mif.FXModel;

import com.mif.common.Register;

import java.lang.reflect.Field;

public class RegisterInstance {

    public Register register;
    public Field field;

    public RegisterInstance(Field f, Register r) {
        this.field = f;
        this.register = r;
    }
}
