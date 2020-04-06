package com.mif.vm;

import com.mif.common.ByteUtil;
import com.mif.exception.OutOfMemoryException;
import com.mif.rm.Processor;
import com.mif.rm.Register;
import com.mif.rm.Memory;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PagingTable {

    // Paging table. Key - page number of virtual memory, value - page number of real memory
    public Map<Integer, Integer> pageMap = new HashMap<>();

    // Uses single instance of memory across all virtual machines (should work?)
    private Memory memoryInstance = Memory.getInstance();

    // 4 bytes (PTR)
    public Register PTR = new Register();

    public PagingTable() {
        Processor.PTR.setValue(memoryInstance.requestPage());
        Processor.SP.setValue(new byte[]{0,0,VirtualMemory.EXTRASEG_START_PAGE - 1,15});
    }

    // Requests unused memory pages from real memory
    public void requestPages(int pageCount) {
        if (memoryInstance.getFreePagesCount() < pageCount)
            throw new OutOfMemoryException("Nebeliko laisvos atminties");
        int newPageCount = pageMap.size() + pageCount;
        for (int i = pageMap.size(); i < newPageCount; i++) {
            int realMemoryPage = memoryInstance.requestPage();
            if (realMemoryPage != -1) {
                pageMap.put(i, realMemoryPage);
            }
        }
    }

    public void setPaging() {
        for (int wordNum = 0; wordNum < pageMap.size(); wordNum++) {
            byte[] realPage = ByteUtil.intToBytes(pageMap.get(wordNum));
            memoryInstance.putWord(Processor.PTR.getValueOfSmallerTwoBytes(), wordNum, realPage);
        }
    }

    public byte[] getWordFromMemory(int page, int word) {
        int pageInMemory = ByteUtil.byteToInt(memoryInstance.getWord(Processor.PTR.getValueOfSmallerTwoBytes(), page));
        return memoryInstance.getWord(pageInMemory, word);
    }

    public byte[] getBytesFromMemory(int page, int word, int byteCount) {
        int pageInMemory = ByteUtil.byteToInt(memoryInstance.getWord(Processor.PTR.getValueOfSmallerTwoBytes(), page));
        return memoryInstance.getBytes(pageInMemory, word, byteCount);
    }

    public void putWordToMemory(int pageNum, int wordNum, byte[] word) {
        int pageInMemory = ByteUtil.byteToInt(memoryInstance.getWord(Processor.PTR.getValueOfSmallerTwoBytes(), pageNum));
        memoryInstance.putWord(pageInMemory, wordNum, word);
    }

    public void putBytesToMemory(int pageNum, int wordNum, byte[] words, int byteCount) {
        int pageInMemory = ByteUtil.byteToInt(memoryInstance.getWord(Processor.PTR.getValueOfSmallerTwoBytes(), pageNum));
        memoryInstance.putBytes(pageInMemory, wordNum, words, byteCount);    }

    public void freeMemory() {
        memoryInstance.freeVMMemory(pageMap, Processor.PTR.getValueOfSmallerTwoBytes());
    }

    public void saveVMRegisters(List<Integer> registerValues) {
        int vMRegisterAddress = memoryInstance.setVmRegistersAddress();
        memoryInstance.saveVMRegisters(vMRegisterAddress, registerValues);
    }

    public List<Integer> loadVMRegisters() {
        return memoryInstance.loadVMRegisters();
    }
}
