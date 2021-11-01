package com.sync.imusic;

import static com.sync.imusic.AddToPlaylistPopup.PLAYLIST_NAME;

import android.content.Context;

public class CheckDb extends Thread{
    private Context mContext;

    public CheckDb (Context mContext){
        this.mContext = mContext;
    }


    @Override
    public void run() {
        DataBaseHelperHistory dataBaseHelperHistory =  new DataBaseHelperHistory(mContext, "history.db", null, 1);
        dataBaseHelperHistory.check();
        DataBaseHelperPlaylist dataBaseHelperPlaylist = new DataBaseHelperPlaylist(mContext, PLAYLIST_NAME, null, 1);
        dataBaseHelperPlaylist.check();
    }

}
