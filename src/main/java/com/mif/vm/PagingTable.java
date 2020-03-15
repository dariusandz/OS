package com.mif.vm;

import com.mif.rm.Memory;

import java.util.HashMap;
import java.util.Map;

public class PagingTable {

    public Map<Integer, Integer> pageMap = new HashMap<>();
    private Memory memoryInstance = Memory.getInstance();

    public PagingTable() { }

    public void requestPages(int pageCount) {
        for (int i = 0; i < pageCount; i++) {
            int realMemoryPage = memoryInstance.requestPage();
            if (realMemoryPage != -1)
                pageMap.put(i, realMemoryPage);
        }
    }

    public byte[] getWordFromMemory(int page, int word) {
        int pageInMemory = pageMap.get(page);
        return memoryInstance.getWord(pageInMemory, word);
    }

    public void putWordToMemory(int pageNum, int wordNum, byte[] word) {
        int pageInMemory = pageMap.get(pageNum);
        memoryInstance.putWord(pageInMemory, wordNum, word);
    }
}
