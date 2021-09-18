package com.example.imusic;

import java.io.File;
import java.util.Comparator;

public class MySortByName_FileActivity implements Comparator<File> {
    private String sortOrder;

    MySortByName_FileActivity(){

    }
    MySortByName_FileActivity(String sortOrder) {
        this.sortOrder = sortOrder;
    }
    @Override
    public int compare(File o1, File o2) {
        String s1 = o1.getName().toUpperCase();
        String s2 = o2.getName().toUpperCase();
        if (sortOrder.equals("DES")) return -s1.compareTo(s2);
        return s1.compareTo(s2);
    }
}
