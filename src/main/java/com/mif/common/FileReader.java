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
            //inputStream = getClass().getClassLoader().getResourceAsStream(fileName);
            //programString = IOUtils.toString(inputStream, "UTF-8");
            inputStream = getClass().getClassLoader().getResourceAsStream("HDD.txt");
            String hddString = IOUtils.toString(inputStream, "UTF-8");
            programString = hddString.split("@" + fileName + "\r\n")[1]; // nezinau ar veiks ant mac
            programString = programString.split("\n!",2)[0];
        } catch (IOException e) {
            e.printStackTrace();
        }
        return programString.replaceAll("\n", "").replace("\r","");
    }
}
