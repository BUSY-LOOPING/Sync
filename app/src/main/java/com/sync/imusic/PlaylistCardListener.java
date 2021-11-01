package com.sync.imusic;

public interface PlaylistCardListener {
    void itemClick(SameNamePlaylistFiles playlistFiles, SameNameItemRecyclerAdapter adapter, int pos);
    void fabClick(SameNamePlaylistFiles playlistFiles);
    void moreClick(SameNamePlaylistFiles sameNamePlaylistFile, SameNameItemRecyclerAdapter adapter);
    void longPress();
}
