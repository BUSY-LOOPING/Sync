package com.example.imusic;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public interface PlaylistContentListener {
    /**
     * Called when a view is requesting a start of a drag.
     *
     * @param viewHolder The holder of the view to drag.
     */
    void onStartDrag(RecyclerView.ViewHolder viewHolder);

    void onClick(ArrayList<PlaylistFiles> playlistFiles, int pos);

    void moreClick();

    void longClick();
}
