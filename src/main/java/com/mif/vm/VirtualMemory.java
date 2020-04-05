package com.mif.vm;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import com.mif.common.ByteUtil;
import com.mif.exception.OutOfMemoryException;
import com.mif.rm.Processor;
import org.apache.commons.io.IOUtils;

public class VirtualMemory implements IMemory {

    private static final int PARAMSEG_START_PAGE = 0;
    public static final int DATASEG_START_PAGE = 1;
    public static final int EXTRASEG_START_PAGE = 4;
    private static final int CODESEG_START_PAGE = 5;

    private static int pageSize = 16;
    private static int wordSize = 4;
    private static int hexSize = 8;

    private static int bytesForParameters = (DATASEG_START_PAGE - PARAMSEG_START_PAGE) * pageSize * wordSize;

    public int pages = 16;
    public static int wordsPerPage = 16;

    public PagingTable pagingTable = new PagingTable();

    public VirtualMemory(List<String> params) {
        //pagingTable.requestPages(pages);
        pagingTable.requestPages(EXTRASEG_START_PAGE); // requestina visus apart codeseg puslapiu
        pagingTable.setPaging();
        putParamsIntoMemory(params);
        Processor.TI.setValue(20);
        Processor.DI.setValue(0);
        Processor.DS.setValue(0);
    }

    // Gets a word(command) to execute from CODESEG
    public byte[] getCodeWord(int nthWord) {
        return pagingTable.getWordFromMemory(CODESEG_START_PAGE + nthWord/16, nthWord%16);
    }

    // Gets a word from memory
    public byte[] getWordFromMemory(int page, int word) {
        return pagingTable.getWordFromMemory(page, word);
    }

    // Puts a register value to memory
    public byte[] getBytesFromMemory(int page, int word, int byteCount) { return pagingTable.getBytesFromMemory(page,word,byteCount); }

    // Puts a word to DATASEG from reg
    public void putValueToMemory(int page, int word, int value) {
        byte[] byteValue = ByteUtil.intToBytes(value);
        pagingTable.putWordToMemory(page, word, byteValue);
    }

    // Puts a word to memory
    public void putWordToMemory(int pageNum, int wordNum, byte[] word) {
        pagingTable.putWordToMemory(pageNum, wordNum, word);
    }

    public void putBytesToMemory(int pageNum, int wordNum, byte[] words, int byteCount) {
        pagingTable.putBytesToMemory(pageNum, wordNum, words, byteCount);
    }

    // Replaces 8byte hexes to 4byte hexes
    private byte[] replaceHex(String program) {
        List<Byte> byteList = new ArrayList<>();

        for (int i = 0; i < program.length(); i += 4) {
            String command = program.substring(i, i + 4);
            byteList = ByteUtil.appendBytesToByteList(byteList, command.getBytes());
            if (command.contains("LD") || command.contains("SVR") || command.startsWith("J")) {
                byte[] hexbytes = ByteUtil.stringHexToBytes(
                        program.substring(i + 4, i + 12)
                );
                byteList = ByteUtil.appendBytesToByteList(byteList, hexbytes);
                i += 8;
            }
        }

        return ByteUtil.getByteArrayFromByteList(byteList);
    }

    // Loads a program from file into string
    public boolean loadProgram(String filePath) {
        InputStream inputStream = null;
        try {
            inputStream = getClass().getClassLoader().getResourceAsStream(filePath);
            String programStr = IOUtils.toString(inputStream, "UTF-8");
            programStr = programStr.replaceAll("\n", "").replace(" ", "").replace("\r","");
            if(!programStr.contains("@codeseg") || !programStr.contains("@dataseg"))
                return false;
            programStr = programStr.split("@codeseg", 2)[1];
            // TODO gal cia galima isskirti page'us???
            int codeSegmentWordCount = (int)Math.ceil(programStr.length() / 4.0);
            int codeSegmentPageCount = (int)Math.ceil(codeSegmentWordCount / 16.0);
            pagingTable.requestPages(codeSegmentPageCount);
            Processor.PTR.setValue(new byte[] {(byte)(codeSegmentPageCount + EXTRASEG_START_PAGE), 16, Processor.PTR.getByteValue()[2], Processor.PTR.getByteValue()[3]});
            pagingTable.setPaging();
            putIntoMemory(replaceHex(programStr));
        } catch (IOException e) {
            e.printStackTrace();
        }
        return true;
    }

    // Puts loaded program from file to CODESEG
    private void putIntoMemory(byte[] byteCode) {
        for (int i = 0; i < byteCode.length / wordSize; i++) {
            byte[] word = Arrays.copyOfRange(byteCode, i * wordSize, i * wordSize + wordSize);
            putWordToMemory(CODESEG_START_PAGE + i/16, i%16, word);
        }
    }

    public void putParamsIntoMemory(List<String> params) {
        if (params.isEmpty())
            return;

        StringBuilder longStringOfParameters = new StringBuilder();
        params.stream()
            .forEach(param -> longStringOfParameters.append(param));

        if (longStringOfParameters.length() > bytesForParameters)
            throw new OutOfMemoryException("Parameters are too long. Maximum characters for parameters: " + bytesForParameters);

        int wordIndex = 0;
        int substringCounter = 0;
        while (wordIndex < (longStringOfParameters.length() / (double) wordSize)) {
            String param = longStringOfParameters.substring(
                    substringCounter, Math.min(longStringOfParameters.length(), substringCounter + wordSize)
            );
            substringCounter += 4;
            byte[] bytes = param.getBytes(StandardCharsets.UTF_8);
            putBytesToMemory(PARAMSEG_START_PAGE, wordIndex++, param.getBytes(StandardCharsets.UTF_8), param.length());
        }
    }

    public void putDataSegIntoMemory(String dataSeg) {
        byte[] bytes = dataSeg.getBytes();
        if(bytes.length > pageSize*wordSize){
            byte[] bytes1 = Arrays.copyOfRange(bytes,0,pageSize * wordSize - 1);
            putBytesToMemory(DATASEG_START_PAGE, 0, bytes1, bytes1.length);
            bytes1 = Arrays.copyOfRange(bytes, pageSize * wordSize + 1, bytes.length);
            putBytesToMemory(DATASEG_START_PAGE + 1, 0, bytes1, bytes1.length);
        }
        else
            putBytesToMemory(DATASEG_START_PAGE, 0, bytes, bytes.length);
    }

    public void freeMemory() {
        pagingTable.freeMemory();
    }
}
