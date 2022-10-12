package com.calamus.easykorean.models;

public class ReviewModel {
    String userId;
    String username;
    String imageUrl;
    String review;
    int star;
    long time;
    boolean vipUser;

    public ReviewModel(String userId, String username, String imageUrl, String review, int star, long time, boolean vipUser) {
        this.userId=userId;
        this.username = username;
        this.imageUrl = imageUrl;
        this.review = review;
        this.star = star;
        this.time = time;
        this.vipUser=vipUser;
    }

    public String getUserId() {
        return userId;
    }

    public boolean isVipUser() {
        return vipUser;
    }

    public String getUsername() {
        return username;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getReview() {
        return review;
    }

    public int getStar() {
        return star;
    }

    public long getTime() {
        return time;
    }
}
