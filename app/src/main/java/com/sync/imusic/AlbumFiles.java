package com.sync.imusic;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Objects;

public class AlbumFiles implements Serializable {
    private String albumName;
    private long duration;
    private long dateAdded;
    private ArrayList<MusicFiles> musicFiles = new ArrayList<>();

    public AlbumFiles(String albumName, long duration, long dateAdded, ArrayList<MusicFiles> musicFiles) {
        this.albumName = albumName;
        this.duration = duration;
        this.dateAdded = dateAdded;
        this.musicFiles = musicFiles;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public long getDuration() {
        return duration;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(long dateAdded) {
        this.dateAdded = dateAdded;
    }

    public ArrayList<MusicFiles> getMusicFiles() {
        return musicFiles;
    }

    public void setMusicFiles(ArrayList<MusicFiles> musicFiles) {
        this.musicFiles = musicFiles;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        AlbumFiles that = (AlbumFiles) o;
        return albumName.substring(0, 1).equals(that.albumName.substring(0, 1));
    }

    @Override
    public int hashCode() {
        return Objects.hash(albumName, duration, dateAdded, musicFiles);
    }
}
