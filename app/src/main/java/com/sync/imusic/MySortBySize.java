package com.sync.imusic;

import java.util.Comparator;

public class MySortBySize implements Comparator<MusicFiles> {
    @Override
    public int compare(MusicFiles o1, MusicFiles o2) {
        long size1 = Long.parseLong(o1.getSize());
        long size2 = Long.parseLong(o2.getSize());
        return (int)(size1 - size2);
    }
}
