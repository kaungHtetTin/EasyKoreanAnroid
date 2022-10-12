package com.calamus.easykorean.models;

public class TopGamePlayerModel {
    String userId;
    String name;
    String imageUrl;
    int score;

    public TopGamePlayerModel(String userId,String name, String imageUrl, int score) {
        this.userId=userId;
        this.name = name;
        this.imageUrl = imageUrl;
        this.score = score;
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public int getScore() {
        return score;
    }
}
