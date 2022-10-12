package com.calamus.easykorean.models;

public class DramaModel {
    String title;
    String drama_id;
    String image_url;

    public DramaModel(String title, String drama_id, String image_url) {
        this.title = title;
        this.drama_id = drama_id;
        this.image_url = image_url;
    }

    public String getTitle() {
        return title;
    }

    public String getDrama_id() {
        return drama_id;
    }

    public String getImage_url() {
        return image_url;
    }
}
