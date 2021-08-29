package com.calamus.easykorean.models;

public class LikeListModel {
    String userName;
    String imageUrl;
    String isVip;
    String userId;

    public LikeListModel(String userId,String userName, String imageUrl, String isVip) {
        this.userId=userId;
        this.userName = userName;
        this.imageUrl = imageUrl;
        this.isVip = isVip;
    }

    public String getUserId() {
        return userId;
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
