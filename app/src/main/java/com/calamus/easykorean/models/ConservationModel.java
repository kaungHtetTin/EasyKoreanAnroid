package com.calamus.easykorean.models;


public class ConservationModel {
    String userId;
    String userName;
    String imageUrl;
    String message;
    String time;
    String senderId;
    String token;
    int seen;

    String title;

    public ConservationModel(){}

    public ConservationModel(String title) {
        this.title = title;
    }

    public ConservationModel(String userId, String userName, String imageUrl, String message, String time, String senderId, String token, int seen) {
        this.userName = userName;
        this.imageUrl = imageUrl;
        this.message = message;
        this.time = time;
        this.userId=userId;
        this.senderId=senderId;
        this.token=token;
        this.seen=seen;

    }

    public String getTitle() {
        return title;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSeen(int seen) {
        this.seen = seen;
    }

    public int getSeen() {
        return seen;
    }

    public String getToken() {
        return token;
    }

    public String getSenderId() {
        return senderId;
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

    public String getMessage() {
        return message;
    }

    public String getTime() {
        return time;
    }
}
