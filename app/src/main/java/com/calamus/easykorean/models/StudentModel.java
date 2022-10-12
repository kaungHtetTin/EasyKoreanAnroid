package com.calamus.easykorean.models;

public class StudentModel {
    String phone;
    String name;
    String imageUrl;
    String token;
    String friendsJson;

    public StudentModel(){}

    public StudentModel(String phone, String name, String imageUrl, String token){
        this.phone = phone;
        this.name = name;
        this.imageUrl = imageUrl;
        this.token=token;
    }

    public StudentModel(String phone, String name, String imageUrl, String token, String friendsJson) {
        this.phone = phone;
        this.name = name;
        this.imageUrl = imageUrl;
        this.token = token;
        this.friendsJson = friendsJson;
    }

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getToken() {
        return token;
    }

    public String getFriendsJson() {
        return friendsJson;
    }
}
