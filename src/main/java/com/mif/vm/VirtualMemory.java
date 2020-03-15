package com.mif.vm;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mif.common.ByteUtil;
import org.apache.commons.io.IOUtils;

public class VirtualMemory implements IMemory {

    private static final int PARAMSEG_START_PAGE = 0;
    private static final int DATASEG_START_PAGE = 1;
    private static final int CODESEG_START_PAGE = 3;

    private static int pageSize = 16;

    protected static int wordSize = 4;
    protected static int hexSize = 8;

    public static int pages = 16;
    public static int words = 16;

    private PagingTable pagingTable = new PagingTable();

    public VirtualMemory() {
        pagingTable.requestPages(pages);
    }

    // Gets a word from CODESEG
    public byte[] getCodeWord(int nthWord) {
        return pagingTable.getWordFromMemory(CODESEG_START_PAGE, nthWord);
    }

    // Gets a word from DATASEG
    public byte[] getWordFromMemory(int page, int word) {
        return pagingTable.getWordFromMemory(page, word);
    }

    // Puts a word to DATASEG from reg
    public void putValueToMemory(int page, int word, int value) {
        byte[] byteValue = ByteUtil.intToBytes(value);
        pagingTable.putWordToMemory(page, word, byteValue);
    }

    public void putWordToMemory(int pageNum, int wordNum, byte[] word) {
        pagingTable.putWordToMemory(pageNum, wordNum, word);
    }

    private byte[] replaceHex(String program) {
        List<Byte> byteList = new ArrayList<>();

        for (int i = 0; i < program.length(); i += 4) {
            String command = program.substring(i, i + 4);
            byteList = ByteUtil.appendBytesToByteList(byteList, command.getBytes());
            if (command.contains("LD") || command.contains("SVR") || command.startsWith("J")) {
                byte[] hexbytes = ByteUtil.minifyHex(
                        program.substring(i + 4, i + 12)
                );
                byteList = ByteUtil.appendBytesToByteList(byteList, hexbytes);
                i += 8;
            }
        }

        return ByteUtil.getByteArrayFromByteList(byteList);
    }

    // Loads a program from file into string
    public void loadProgram(String filePath) {
        try {
            InputStream inputStream = VirtualMemory.class.getResourceAsStream(filePath);
            String programStr = IOUtils.toString(inputStream, "UTF-8");
            programStr = programStr.replaceAll("\n", "").replace(" ", "");
            putIntoMemory(replaceHex(programStr));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Puts loaded program from file to CODESEG
    private void putIntoMemory(byte[] byteCode) {
        for (int i = 0; i < byteCode.length / wordSize; i++) {
            byte[] word = Arrays.copyOfRange(byteCode, i * wordSize, i * wordSize + wordSize);
            putWordToMemory(CODESEG_START_PAGE, i, word);

            // 8 byte commands, reads next byte for hex value
            String wordStr = new String(word);
            if (wordStr.contains("LD") || wordStr.contains("SVR") || wordStr.startsWith("J")) {
                i++;
                byte[] hexWord = Arrays.copyOfRange(byteCode, i * wordSize, i * wordSize + wordSize);
                putWordToMemory(CODESEG_START_PAGE, i, hexWord);
            }
        }
    }
}
