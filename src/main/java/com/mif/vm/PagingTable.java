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

    public byte[] getBytesFromMemory(int page, int word, int byteCount) {
        int pageInMemory = pageMap.get(page);
        return memoryInstance.getBytes(pageInMemory, word, byteCount);
    }

    public void putWordToMemory(int pageNum, int wordNum, byte[] word) {
        int pageInMemory = pageMap.get(pageNum);
        memoryInstance.putWord(pageInMemory, wordNum, word);
    }

    public void putBytesToMemory(int pageNum, int wordNum, byte[] words, int byteCount) {
        int pageInMemory = pageMap.get(pageNum);
        memoryInstance.putBytes(pageInMemory, wordNum, words, byteCount);    }
}
