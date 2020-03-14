package com.mif.vm;

public interface IMemory {

    String getWord(int displacement);

    void putValueToMemory(int value, int page, int word);
    String getWordFromMemory(int page, int word);
}
