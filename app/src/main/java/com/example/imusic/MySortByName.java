package com.example.imusic;

import java.util.Comparator;

public class MySortByName implements Comparator<MusicFiles> {
    @Override
    public int compare(MusicFiles o1, MusicFiles o2) {
        String s1 = o1.getTitle().toUpperCase();
        String s2 = o2.getTitle().toUpperCase();
        return s1.compareTo(s2);
    }
}
