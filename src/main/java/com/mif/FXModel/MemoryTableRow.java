package com.mif.FXModel;

import com.mif.common.ByteUtil;
import com.mif.rm.Memory;

import java.nio.ByteBuffer;

public class MemoryTableRow {

    public Integer pageNumber;
    public String[] wordHexValues = new String[Memory.pageSize];

    public MemoryTableRow(int pageNumber) { this.pageNumber = pageNumber; }

    public String[] getValues() { return wordHexValues; }

    public Integer getPageNumber() {
        return pageNumber;
    }

    public void setPageNumber(Integer pageNumber) {
        this.pageNumber = pageNumber;
    }

    public String[] getHexValues() {
        return wordHexValues;
    }

    public void setHexValues(String[] hexValues) {
        this.wordHexValues = hexValues;
    }

    public void add(int elIndex, byte[] word) {
        wordHexValues[elIndex] = ByteUtil.bytesToHex(word);
    }

    public String getWord(int wordIndex) {
        return wordHexValues[wordIndex];
    }

    public void setWord(int wordIndex, String value) {
        wordHexValues[wordIndex] = value; }
}
