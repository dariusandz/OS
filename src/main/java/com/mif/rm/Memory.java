package com.mif.rm;

import com.mif.common.ByteUtil;

import java.util.*;

public class Memory {

    public static final int pageSize = 16;
    public static final int wordLen = 4;
    public static final int defaultMemorySize = 4096;
    public static final int VM_REGISTERS_ADDRESS = 0;

    static Memory globalMemory = getInstance();

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
        occupiedPageNumber.add(VM_REGISTERS_ADDRESS);
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

    private int getVMCounter() {
        int counter = 0;
        byte[] bytes;
        while (true) {
            bytes = Arrays.copyOfRange(memory, (counter * wordLen), (counter * wordLen) + wordLen);
            boolean allZeros = true;
            for (byte b : bytes) {
                if (b != 0)
                    allZeros = false;
            }
            if (allZeros) {
                break;
            } else counter++;
        }
        return counter;
    }

    public int setVmRegistersAddress() {
        int counter = getVMCounter();
        byte[] vMAddress = ByteUtil.intToBytes(requestPage());
        for (int i = 0; i < vMAddress.length; i++) {
            memory[counter * wordLen + i] = vMAddress[i];
        }
        return ByteUtil.byteToInt(vMAddress);
    }

    public void saveVMRegisters(int vMRegisterAddress, List<Integer> registerValues) {
        int registerCounter = 0;
        for (Integer registerValue: registerValues) {
            byte[] registerValueInBytes = ByteUtil.intToBytes(registerValue);
            for (int i = 0; i < registerValueInBytes.length; i++) {
                memory[vMRegisterAddress * wordLen * pageSize + registerCounter * wordLen + i] = registerValueInBytes[i];
            }
            registerCounter++;
        }
    }

    public List<Integer> loadVMRegisters() {
        List<Integer> vmRegisters = new ArrayList<>();
        int counter = getVMCounter();
        int oldVMAddress = ByteUtil.byteToInt(Arrays.copyOfRange(memory, (counter - 1) * wordLen, (counter - 1) * wordLen + wordLen));
        for (int i = 0; i < wordLen; i++) {
            memory[(counter - 1) * wordLen + i] = 0;
        }
        occupiedPageNumber.remove(occupiedPageNumber.indexOf(oldVMAddress));
        while (true) {
            if(vmRegisters.size() < 13) {
                byte[] bytes = Arrays.copyOfRange(memory, oldVMAddress * pageSize * wordLen + vmRegisters.size() * wordLen,
                        oldVMAddress * pageSize * wordLen + vmRegisters.size() * wordLen + wordLen);
                for (int i = 0; i < wordLen; i++) {
                    memory[oldVMAddress * pageSize * wordLen + vmRegisters.size() * wordLen + i] = 0;
                }
                vmRegisters.add(ByteUtil.byteToInt(bytes));
            }
            else break;
        }
        return vmRegisters;
    }
}
