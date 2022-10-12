package com.calamus.easykorean.models;

public class ExtraCourseModel {
    String title;
    String id;
    String image_url;

    public ExtraCourseModel(String title, String id, String image_url) {
        this.title = title;
        this.id = id;
        this.image_url = image_url;
    }

    public String getTitle() {
        return title;
    }

    public String getId() {
        return id;
    }

    public String getImage_url() {
        return image_url;
    }
}
