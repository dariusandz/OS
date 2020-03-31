package com.mif.common;

import com.mif.vm.VirtualMemory;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.io.InputStream;

public class FileReader {

    public String loadProgramAsString(String fileName) {
        String programString = "";
        InputStream inputStream = null;
        try {
            inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
            programString = IOUtils.toString(inputStream, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        return programString.replaceAll("\n", "").replace(" ", "").replace("\r","");
    }
}
