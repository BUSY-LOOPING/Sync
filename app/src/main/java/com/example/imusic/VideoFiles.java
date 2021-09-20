package com.example.imusic;


import java.io.Serializable;
import java.util.Objects;

public class VideoFiles implements Serializable {
    private String id;
    private String path;
    private String title;
    private String filename;
    private String size;
    private String dateAdded;
    private String duration;
    private String resolution;
    private String resolutionInGeneral;

    public VideoFiles() {
    }

    public VideoFiles(String id, String path, String title, String filename, String size, String dateAdded, String duration, String resolution) {
        this.id = id;
        this.path = path;
        this.title = title;
        this.filename = filename;
        this.size = size;
        this.dateAdded = dateAdded;
        this.duration = duration;
        this.resolution = resolution;
        String[] arr = new String[0];
        if (resolution != null)
            arr = resolution.split("x");
        if (arr.length == 2) {
            if (Integer.parseInt(arr[1]) == 360)
                resolutionInGeneral = "360p";
            if (Integer.parseInt(arr[1]) == 480)
                resolutionInGeneral = "480p";
            if (Integer.parseInt(arr[1]) == 720)
                resolutionInGeneral = "720p";
            if (Integer.parseInt(arr[1]) == 1080)
                resolutionInGeneral = "1080p";
            if (Integer.parseInt(arr[1]) > 1080 && Integer.parseInt(arr[0]) > 1920 && Integer.parseInt(arr[0]) < 3840)
                resolutionInGeneral = "2K";
            if (Integer.parseInt(arr[1]) > 1080 && Integer.parseInt(arr[0]) >= 2560 && Integer.parseInt(arr[0]) < 7680)
                resolutionInGeneral = "4K";
            if (Integer.parseInt(arr[1]) > 1080 && Integer.parseInt(arr[0]) >= 7680)
                resolutionInGeneral = "8K";
        }
    }

    public String getResolution() {
        return resolution;
    }

    public void setResolution(String resolution) {
        this.resolution = resolution;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public String getFilename() {
        return filename;
    }

    public void setFilename(String filename) {
        this.filename = filename;
    }

    public String getSize() {
        return size;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public String getDateAdded() {
        return dateAdded;
    }

    public void setDateAdded(String dateAdded) {
        this.dateAdded = dateAdded;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getResolutionInGeneral() {
        return resolutionInGeneral;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof VideoFiles)) return false;
        VideoFiles that = (VideoFiles) o;
        return getId().equals(that.getId()) &&
                getPath().equals(that.getPath()) &&
                getTitle().equals(that.getTitle()) &&
                getFilename().equals(that.getFilename()) &&
                getSize().equals(that.getSize()) &&
                getDateAdded().equals(that.getDateAdded()) &&
                getDuration().equals(that.getDuration()) &&
                getResolution().equals(that.getResolution());
    }


    @Override
    public int hashCode() {
        return Objects.hash(getId(), getPath(), getTitle(), getFilename(), getSize(), getDateAdded(), getDuration(), getResolution(), getResolutionInGeneral());
    }
}
