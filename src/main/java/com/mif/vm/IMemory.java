package com.mif.vm;

public interface IMemory {

    byte[] getCodeWord(int nthWord);
    byte[] getWordFromMemory(int page, int word);
    byte[] getBytesFromMemory(int page, int word, int wordCount);

    void putValueToMemory(int page, int word, int value);
    void putWordToMemory(int pageNum, int wordNum, byte[] word);

    void loadProgram(String filename);
    void putBytesToMemory(int pageNum, int wordNum, byte[] words, int byteCount);
}
