package com.mif.vm;

import java.io.*;
import java.util.Arrays;

import com.mif.common.Util;
import com.sun.tools.javac.util.ArrayUtils;
import org.apache.commons.io.IOUtils;

public class VirtualMemory implements IMemory {

    private static int pageSize = 16;

    protected static int wordSize = 4;
    protected static int hexSize = 8;

    public static int pages = 16;
    public static int words = 16;

    public byte[] memory = new byte[wordSize * pages * words];

    public VirtualMemory() { Arrays.fill(memory, (byte) 0); }

    // Gets a word from CODESEG
    public String getWord(int displacement) {
        StringBuilder sb = new StringBuilder();
        for (int i = displacement; i < displacement + wordSize; i++)
            sb.append((char) memory[i]);
        return sb.toString();
    }

    // Gets a word from DATASEG
    public String getWordFromMemory(int page, int word) {
        int locationInMemory = page * pageSize + word;
        byte[] bytesInMemory = Arrays.copyOfRange(memory, locationInMemory, locationInMemory + 4);
        return Util.bytesToString(bytesInMemory);
    }

    // Puts a word to DATASEG from reg
    public void putValueToMemory(int value, int page, int word) {
        byte[] byteValue = Util.intToBytes(value);
        int byteIndex = 0;
        for (int i = page * pageSize + word; i < page * pageSize + word + wordSize; i++)
            memory[i] = byteValue[byteIndex++];
    }

    private String getHexWord(int displacement, String code) {
        return code.substring(displacement, displacement + 8);
    }

    private byte[] getHexBytes(String word) {
        byte[] hexBytes = new byte[4];
        for (int i = 0; i < word.length(); i += 2) {
            hexBytes[i / 2] = (byte) ((Character.digit(word.charAt(i), 16) << 4)
                                        + Character.digit(word.charAt(i+1), 16));
        }
        return hexBytes;
    }

    // Loads a program from file into string
    public void loadProgram(String filePath) {
        try {
            InputStream inputStream = VirtualMemory.class.getResourceAsStream(filePath);
            String programStr = IOUtils.toString(inputStream, "UTF-8");
            programStr = programStr.replaceAll("\n", "").replace(" ", "");
            putIntoMemory(programStr);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Puts loaded program from file to CODESEG
    private void putIntoMemory(String code) {
        for (int i = 0; i < code.length(); i += 4) {
            String word = code.substring(i, i + 4);
            putBytesIntoMemory(word.getBytes(), i);

            // 8 Baitu komandos
            if (word.contains("LD") || word.contains("SVR") || word.startsWith("J")) {
                String hexWord = getHexWord(i + 4, code);
                byte[] hexBytes = getHexBytes(hexWord);
                putBytesIntoMemory(hexBytes, i + 4);
                i += 8;
            }
        }
    }

    // Puts a program word as bytes into CODESEG
    private void putBytesIntoMemory(byte[] word, int displacement) {
        int byteProcessed = 0;
        for (int i = displacement; i < displacement + word.length; i++)
            this.memory[i] = word[byteProcessed++];
    }
}
