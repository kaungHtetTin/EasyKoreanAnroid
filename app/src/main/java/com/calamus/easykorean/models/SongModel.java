package com.calamus.easykorean.models;

import android.graphics.Bitmap;
import android.net.Uri;

public class SongModel {
    private final Uri uri;
    private final String title;
    private final int duration;
    private final Bitmap thumbnail;

    public SongModel(Uri uri, String title, int duration, Bitmap thumbnail) {
        this.uri = uri;
        this.title = title;
        this.duration = duration;
        this.thumbnail = thumbnail;
    }

    public Uri getUri() {
        return uri;
    }

    public String getTitle() {
        return title;
    }

    public int getDuration() {
        return duration;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }
}
