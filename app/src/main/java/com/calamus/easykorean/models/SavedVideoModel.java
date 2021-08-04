package com.calamus.easykorean.models;

import android.graphics.Bitmap;
import android.net.Uri;

public class SavedVideoModel {

    private final Uri uri;
    private final String name;
    private final int duration;
    private final int size;
    private final Bitmap thumbnail;

    public SavedVideoModel(Uri uri, String name, int duration, int size, Bitmap thumbnail) {
        this.uri = uri;
        this.name = name;
        this.duration = duration;
        this.size = size;
        this.thumbnail = thumbnail;
    }

    public Uri getUri() {
        return uri;
    }

    public String getName() {
        return name;
    }

    public int getDuration() {
        return duration;
    }

    public int getSize() {
        return size;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }
}
