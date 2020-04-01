package com.mif.FXModel;

import com.mif.rm.Memory;

public class PagingTableRow {

    private Integer[] realPages = new Integer[Memory.pageSize];

    public void add(int elIndex, Integer pageInRealMemory) {
        realPages[elIndex] = pageInRealMemory;
    }

    public Integer[] getRmPages() { return realPages; }

}
