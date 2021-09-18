package com.example.imusic;


import java.io.Serializable;
import java.util.Objects;

public class MusicFiles implements Serializable {

    private String path;
    private String title;
    private String artist;
    private String album;
    private String duration;
    private String id;
    private String size;

    public MusicFiles() {
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getAlbum() {
        return album;
    }

    public void setAlbum(String album) {
        this.album = album;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getId () {
        return id;
    }

    public void setId (String id) {
        this.id = id;
    }


    public MusicFiles(String path, String title, String artist, String album, String duration, String id, String size) {
        this.path = path;
        this.title = title;
        this.artist = artist;
        this.album = album;
        this.duration = duration;
        this.id = id;
        this.size = size;
        if (this.artist == null || this.artist.equals("unknown"))  this.artist = "<unknown>";
        if (this.album == null || this.album.equals("unknown")) this.album = "<unknown>";
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MusicFiles that = (MusicFiles) o;
        return getPath().equals(that.getPath()) &&
                getTitle().equals(that.getTitle()) &&
                getArtist().equals(that.getArtist()) &&
                getAlbum().equals(that.getAlbum()) &&
//                getDuration().equals(that.getDuration()) &&
                getId().equals(that.getId()) &&
                getSize().equals(that.getSize());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getPath(), getTitle(), getArtist(), getAlbum(), getDuration(), getId(), getSize());
    }
}
