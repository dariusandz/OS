package com.mif.rm;

import java.util.*;

public class Memory {

    public static final int pageSize = 16;
    public static final int wordLen = 4;
    public static final int defaultMemorySize = 4096;

    private static Memory globalMemory = getInstance();

    private List<Integer> occupiedPageNumber = new ArrayList<>();
    private byte[] memory;

    public static Memory getInstance() {
        if (globalMemory == null)
            globalMemory = new Memory();

        return globalMemory;
    }

    private Memory() {
        memory = new byte[defaultMemorySize];
        Arrays.fill(memory, (byte) 0);
    }

    public int requestPage() {
        Random r = new Random();
        while (true) {
            int randomPage = r.nextInt(memory.length / (pageSize * wordLen));
            if (!occupiedPageNumber.contains(randomPage)) {
                occupiedPageNumber.add(randomPage);
                return randomPage;
            }
        }
    }

    public byte[] getWord(int page, int word) {
        return Arrays.copyOfRange(memory, page * pageSize * wordLen + word * wordLen, page * pageSize * wordLen + word * wordLen + wordLen);
    }

    public byte[] getBytes(int page, int word, int byteCount) {
        return Arrays.copyOfRange(memory, page * pageSize * wordLen + word * wordLen, page * pageSize * wordLen + word * wordLen + byteCount);
    }

    public void putWord(int pageNum, int wordNum, byte[] word) {
        int byteIndex = 0;
        for (int i = pageNum * pageSize * wordLen + wordNum * wordLen; i < pageNum * pageSize * wordLen + wordNum * wordLen + wordLen; i++) {
            memory[i] = word[byteIndex++];
        }
    }

    public void putBytes(int pageNum, int wordNum, byte[] words, int byteCount) {
        int byteIndex = 0;
        for (int i = pageNum * pageSize * wordLen + wordNum * wordLen; i < pageNum * pageSize * wordLen + wordNum * wordLen + byteCount; i++) {
            memory[i] = words[byteIndex++];
        }
    }

    public void freeVMMemory(Map<Integer, Integer> pageMap, int ptrValue) {
        for (Map.Entry<Integer, Integer> page: pageMap.entrySet()) {
            occupiedPageNumber.remove(occupiedPageNumber.indexOf(page.getValue()));
            for (int i = page.getValue() * pageSize * wordLen; i < (page.getValue() + 1) * pageSize * wordLen; i++) {
                memory[i] = 0;
            }
        }
        occupiedPageNumber.remove(occupiedPageNumber.indexOf(ptrValue));
        for (int i =  ptrValue * pageSize * wordLen; i < (ptrValue + 1) * pageSize * wordLen; i++) {
            memory[i] = 0;
        }
    }
}
