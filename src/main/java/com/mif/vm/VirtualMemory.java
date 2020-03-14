package com.mif.vm;

import java.io.*;
import java.util.Arrays;

import com.sun.tools.javac.util.ArrayUtils;
import org.apache.commons.io.IOUtils;

public class VirtualMemory implements IMemory {

    protected static int wordSize = 4;
    protected static int hexSize = 8;

    public static int pages = 16;
    public static int words = 16;

    public byte[] memory = new byte[wordSize * pages * words];

    public VirtualMemory() { }

    public String getWord(int displacement) {
        StringBuilder sb = new StringBuilder();
        for (int i = displacement; i < displacement + wordSize; i++)
            sb.append((char) memory[i]);
        return sb.toString();
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

    private void putBytesIntoMemory(byte[] word, int displacement) {
        int byteProcessed = 0;
        for (int i = displacement; i < displacement + word.length; i++)
            this.memory[i] = word[byteProcessed++];
    }
}
