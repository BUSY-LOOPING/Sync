package com.example.imusic;

import androidx.recyclerview.widget.RecyclerView;

public interface PlaylistContentListener {
    /**
     * Called when a view is requesting a start of a drag.
     *
     * @param viewHolder The holder of the view to drag.
     */
    void onStartDrag(RecyclerView.ViewHolder viewHolder);

    void onClick();

    void moreClick();

    void longClick();
}
