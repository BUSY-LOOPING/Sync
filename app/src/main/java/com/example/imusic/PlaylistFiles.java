package com.example.imusic;

import android.os.Build;

import androidx.annotation.RequiresApi;

import java.io.Serializable;
import java.util.Objects;

public class PlaylistFiles implements Serializable {
    public boolean isMusicFile = false;
    public boolean isVideoFile = false;

    private String playlistName;
    private MusicFiles musicFiles;
    private VideoFiles videoFiles;

    public PlaylistFiles(MusicFiles musicFiles, String playlistName) {
        this.musicFiles = musicFiles;
        this.playlistName = playlistName;
        isMusicFile = true;
        isVideoFile = false;
    }

    public PlaylistFiles(VideoFiles videoFiles, String playlistName) {
        this.videoFiles = videoFiles;
        this.playlistName = playlistName;
        isVideoFile = true;
        isMusicFile = false;
    }

    public PlaylistFiles(String playlistName){
        this.playlistName = playlistName;
        musicFiles = new MusicFiles();
        videoFiles = new VideoFiles();
        isVideoFile = false;
        isMusicFile = false;
    }

    public String getPlaylistName() {
        return playlistName;
    }

    public void setPlaylistName(String playlistName) {
        this.playlistName = playlistName;
    }

    public MusicFiles getMusicFiles() {
        return musicFiles;
    }

    public void setMusicFiles(MusicFiles musicFiles) {
        this.musicFiles = musicFiles;
    }

    public VideoFiles getVideoFiles() {
        return videoFiles;
    }

    public void setVideoFiles(VideoFiles videoFiles) {
        this.videoFiles = videoFiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof PlaylistFiles)) return false;
        PlaylistFiles that = (PlaylistFiles) o;
        return isMusicFile == that.isMusicFile &&
                isVideoFile == that.isVideoFile &&
                getPlaylistName().equals(that.getPlaylistName());
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public int hashCode() {
        return Objects.hash(isMusicFile, isVideoFile, getPlaylistName());
    }
}
