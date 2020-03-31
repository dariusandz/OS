package com.mif.FXModel;

import com.mif.rm.Memory;

import java.nio.ByteBuffer;

public class MemoryTableRow {
//TODO make hex values
    public Integer pageNumber;
    public Integer[] wordIntValues = new Integer[Memory.pageSize];

    public MemoryTableRow(int pageNumber) { this.pageNumber = pageNumber; }

    public Integer[] getValues() { return wordIntValues; }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public Integer[] getIntValues() {
        return wordIntValues;
    }

    public void setIntValues(Integer[] intValues) {
        this.wordIntValues = intValues;
    }

    public void add(int elIndex, byte[] word) {
        wordIntValues[elIndex] = ByteBuffer.wrap(word).getInt();
    }

    public Integer getWord(int wordIndex) {
        return wordIntValues[wordIndex];
    }

    public void setWord(int wordIndex, int value) {
        wordIntValues[wordIndex] = value;
    }
}
