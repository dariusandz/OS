package com.mif.vm;

import java.io.*;
import java.util.Arrays;

import com.sun.tools.javac.util.ArrayUtils;
import org.apache.commons.io.IOUtils;

public class VirtualMemory implements IMemory {

    public static int wordSize = 4;

    public static int pages = 16;
    public static int words = 16;

    public byte[] memory = new byte[wordSize * pages * words];

    public VirtualMemory() { }

    public String getCommand(int displacement) {
        StringBuilder sb = new StringBuilder();
        for (int i = displacement; i < displacement + wordSize; i++)
            sb.append((char) memory[i]);
        return sb.toString();
    }

    public void loadProgram(String filePath) {
        try {
            InputStream inputStream = VirtualMemory.class.getResourceAsStream(filePath);
            String programStr = IOUtils.toString(inputStream, "UTF-8");
            programStr = programStr.replaceAll("\n", "").replace(" ", "");
            this.memory = programStr.getBytes();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
