package com.mif.vm;

import com.mif.rm.Memory;

import java.util.HashMap;
import java.util.Map;

public class PagingTable {

    // Paging table. Key - page number of virtual memory, value - page number of real memory
    public Map<Integer, Integer> pageMap = new HashMap<>();

    // Uses single instance of memory across all virtual machines (should work?)
    private Memory memoryInstance = Memory.getInstance();

    public PagingTable() { }

    // Requests unused memory pages from real memory
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
