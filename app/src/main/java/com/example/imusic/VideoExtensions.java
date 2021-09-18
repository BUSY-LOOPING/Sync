package com.example.imusic;

public class VideoExtensions {
    public String [] extensions = {".mp4", ".mkv", "mp4a", };

    public boolean isPresent(String extension) {
        for (String s : extensions)
        {
            if (extension.endsWith(s))
            {
                return true;
            }
        }
        return false;
    }

    public String replaceExtension(String string)
    {
        for (String s : extensions)
        {
            if (string.endsWith(s))
            {
                string = string.replace(s, "");
            }
        }
        return string;
    }
}
