package com.mif.rm;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class Memory {

    private static final int pageSize = 16;
    private static final int wordLen = 4;
    private static final int defaultMemorySize = 1024;

    private static Memory globalMemory = null;

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
            int randomPage = r.nextInt(memory.length / pageSize);
            if (!occupiedPageNumber.contains(randomPage)) {
                occupiedPageNumber.add(randomPage);
                return randomPage;
            }
        }
    }

    public byte[] getWord(int page, int word) {
        return Arrays.copyOfRange(memory, page * pageSize + word * wordLen, page * pageSize + word * wordLen + wordLen);
    }

    public byte[] getBytes(int page, int word, int byteCount) {
        return Arrays.copyOfRange(memory, page * pageSize + word * wordLen, page * pageSize + word * wordLen + byteCount);
    }

    public void putWord(int pageNum, int wordNum, byte[] word) {
        int byteIndex = 0;
        for (int i = pageNum * pageSize + wordNum * wordLen; i < pageNum * pageSize + wordNum * wordLen + wordLen; i++) {
            memory[i] = word[byteIndex++];
        }
    }

    public void putBytes(int pageNum, int wordNum, byte[] words, int byteCount) {
        int byteIndex = 0;
        for (int i = pageNum * pageSize + wordNum * wordLen; i < pageNum * pageSize + wordNum * wordLen + byteCount; i++) {
            memory[i] = words[byteIndex++];
        }
    }
}
