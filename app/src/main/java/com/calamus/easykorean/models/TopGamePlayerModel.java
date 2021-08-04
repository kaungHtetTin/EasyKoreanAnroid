package com.calamus.easykorean.models;

public class TopGamePlayerModel {
    String name;
    String imageUrl;
    String score;

    public TopGamePlayerModel(String name, String imageUrl, String score) {
        this.name = name;
        this.imageUrl = imageUrl;
        this.score = score;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getScore() {
        return score;
    }
}
