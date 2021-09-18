package com.example.imusic;

import android.view.View;

public interface OnItemClickListenerMusicAdapter {
    void onItemClick (int position);
    void onMoreClick (int position, View v);
    void onLongPressed (int position);
}
