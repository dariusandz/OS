package com.mif.vm;

import com.mif.common.ByteUtil;
import com.mif.common.Register;
import com.mif.rm.Memory;

import java.util.HashMap;
import java.util.Map;

public class PagingTable {

    // Paging table. Key - page number of virtual memory, value - page number of real memory
    public Map<Integer, Integer> pageMap = new HashMap<>();

    // Uses single instance of memory across all virtual machines (should work?)
    private Memory memoryInstance = Memory.getInstance();

    // 4 bytes (PTR)
    public Register PTR = new Register();

    public PagingTable() {
        PTR.setValue(memoryInstance.requestPage());
    }

    // Requests unused memory pages from real memory
    public void requestPages(int pageCount) {
        for (int i = 0; i < pageCount; i++) {
            int realMemoryPage = memoryInstance.requestPage();
            if (realMemoryPage != -1) {
                pageMap.put(i, realMemoryPage);
            }
        }
    }

    public void setPaging() {
        for (int wordNum = 0; wordNum < pageMap.size(); wordNum++) {
            byte[] realPage = ByteUtil.intToBytes(pageMap.get(wordNum));
            memoryInstance.putWord(PTR.getValue(), wordNum, realPage);
        }
    }

    public byte[] getWordFromMemory(int page, int word) {
        int pageInMemory = ByteUtil.byteToInt(memoryInstance.getWord(PTR.getValue(), page));
        return memoryInstance.getWord(pageInMemory, word);
    }

    public void putWordToMemory(int pageNum, int wordNum, byte[] word) {
        int pageInMemory = ByteUtil.byteToInt(memoryInstance.getWord(PTR.getValue(), pageNum));
        memoryInstance.putWord(pageInMemory, wordNum, word);
    }
}
