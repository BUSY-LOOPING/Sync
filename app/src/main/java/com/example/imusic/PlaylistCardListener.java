package com.example.imusic;

public interface PlaylistCardListener {
    void itemClick(SameNamePlaylistFiles playlistFiles);
    void fabClick();
    void moreClick(SameNamePlaylistFiles sameNamePlaylistFile, SameNameItemRecyclerAdapter adapter);
    void longPress();
}
