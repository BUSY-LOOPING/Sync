package com.example.imusic;

import static com.example.imusic.AddToPlaylistPopup.PLAYLIST_NAME;

import android.content.Context;
import android.os.AsyncTask;

import java.util.ArrayList;

public class AddPlaylistFilesToDb extends AsyncTask<String, String, String> {
    private ArrayList<MusicFiles> musicFiles;
    private ArrayList<VideoFiles> videoFiles;
    private DataBaseHelperPlaylist db;
    private String playlistName;
    public static boolean ready = true;

    public AddPlaylistFilesToDb(Context mContext) {
        db = new DataBaseHelperPlaylist(mContext, PLAYLIST_NAME, null, 1);
    }

    public void set(ArrayList<MusicFiles> musicFiles, ArrayList<VideoFiles> videoFiles, String playlistName) {
        this.musicFiles = musicFiles;
        this.videoFiles = videoFiles;
        this.playlistName = playlistName;
        ready = false;
    }

    @Override
    protected void onPreExecute() {
        ready = false;
    }

    @Override
    protected String doInBackground(String... strings) {
        addToPlayList(playlistName);
        return null;
    }

    private void addToPlayList(String playlistName) {
        if (!playlistName.equals("")) {
            if (musicFiles.size() > 0) {
                for (MusicFiles temp : musicFiles) {
                    db.insertData(true,
                            false,
                            temp.getPath(),
                            temp.getTitle(),
                            temp.getArtist(),
                            temp.getAlbum(),
                            temp.getDuration(),
                            temp.getId(),
                            temp.getSize(),
                            "",
                            "",
                            "",
                            playlistName);
                }
            }

            if (videoFiles.size() > 0) {
                for (VideoFiles temp : videoFiles) {
                    db.insertData(false,
                            true,
                            temp.getPath(),
                            temp.getTitle(),
                            "",
                            "",
                            temp.getDuration(),
                            temp.getId(),
                            temp.getSize(),
                            temp.getFilename(),
                            temp.getDateAdded(),
                            temp.getResolution(),
                            playlistName);
                }
            }
            if (musicFiles.size() == 0 && videoFiles.size() == 0) {
                db.insertData(false,
                        false,
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        playlistName);
            }
        }
    }

    @Override
    protected void onPostExecute(String s) {
        db.close();
        ready = true;
    }
}
