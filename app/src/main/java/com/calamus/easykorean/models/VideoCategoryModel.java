package com.calamus.easykorean.models;

public class VideoCategoryModel {
    String category;
    String id;

    public VideoCategoryModel(String category, String id) {
        this.category = category;
        this.id = id;
    }

    public String getCategory() {
        return category;
    }

    public String getId() {
        return id;
    }
}
