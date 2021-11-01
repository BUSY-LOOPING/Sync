package com.sync.imusic;

import androidx.annotation.Nullable;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class SameNamePlaylistFiles implements Serializable {
    private ArrayList<MusicFiles> musicFiles;
    private ArrayList<VideoFiles> videoFiles;
    private String playListName;
    private boolean isEmpty = false;
    private int size;
    private static int id = 0;

    public SameNamePlaylistFiles(String playListName) {
        this.playListName = playListName;
        isEmpty = true;
        id++;
    }

    public SameNamePlaylistFiles() {
        musicFiles = new ArrayList<>();
        videoFiles = new ArrayList<>();
        id++;
    }

    public SameNamePlaylistFiles(ArrayList<MusicFiles> musicFiles, ArrayList<VideoFiles> videoFiles, String playListName) {
        this.musicFiles = musicFiles;
        this.videoFiles = videoFiles;
        this.playListName = playListName;
        if (musicFiles.size() == 0 && videoFiles.size() == 0)
            isEmpty = true;
        id++;
    }

    public SameNamePlaylistFiles(PlaylistFiles playlistFiles) {
        musicFiles = new ArrayList<>();
        videoFiles = new ArrayList<>();
        if (playlistFiles.getPlaylistName() != null)
            this.playListName = playlistFiles.getPlaylistName();
            if (playlistFiles.getMusicFiles() != null && playlistFiles.isMusicFile)
                musicFiles.add(playlistFiles.getMusicFiles());
            if (playlistFiles.getVideoFiles() != null && playlistFiles.isVideoFile)
                videoFiles.add(playlistFiles.getVideoFiles());
            id++;
    }

    public ArrayList<MusicFiles> getMusicFiles() {
        return musicFiles;
    }

    public void setMusicFiles(ArrayList<MusicFiles> musicFiles) {
        this.musicFiles = musicFiles;
    }

    public ArrayList<VideoFiles> getVideoFiles() {
        return videoFiles;
    }

    public void setVideoFiles(ArrayList<VideoFiles> videoFiles) {
        this.videoFiles = videoFiles;
    }

    public String getPlayListName() {
        return playListName;
    }

    public void setPlayListName(String playListName) {
        this.playListName = playListName;
    }

    public boolean isEmpty() {
        return isEmpty;
    }

    public void add(PlaylistFiles playlistFiles) {
        if (playlistFiles.getMusicFiles() != null)
            musicFiles.add(playlistFiles.getMusicFiles());
        if (playlistFiles.getVideoFiles() != null)
            videoFiles.add(playlistFiles.getVideoFiles());
    }

    public int getSize() {
        return musicFiles.size() + videoFiles.size();
    }

    @Override
    public boolean equals(@Nullable Object obj) {
        if (obj == this) return true;
        SameNamePlaylistFiles s = (SameNamePlaylistFiles) obj;
        if (s != null && this.playListName != null) {
            return (s.getPlayListName().equals(this.playListName));
        }
        return false;
    }

    @Override
    public int hashCode() {
        return Objects.hash(playListName);
    }
}
