package com.example.imusic;

import java.io.File;
import java.util.Comparator;

public class MySortBySize_FileActivity implements Comparator<File> {
    private String sortOrder;

    MySortBySize_FileActivity() {

    }

    MySortBySize_FileActivity(String sortOrder) {
        this.sortOrder = sortOrder;
    }

    @Override
    public int compare(File o1, File o2) {
        long res = 0;
        if (o1.length() != o2.length()) res = o1.length() - o2.length();
        if (sortOrder.equals("DES")) return -(int) res;
        return (int) res;
    }
}
