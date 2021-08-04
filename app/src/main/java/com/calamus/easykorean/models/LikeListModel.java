package com.calamus.easykorean.models;

public class LikeListModel {
    String userName;
    String imageUrl;
    String isVip;

    public LikeListModel(String userName, String imageUrl, String isVip) {
        this.userName = userName;
        this.imageUrl = imageUrl;
        this.isVip = isVip;
    }

    public String getUserName() {
        return userName;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getIsVip() {
        return isVip;
    }
}
