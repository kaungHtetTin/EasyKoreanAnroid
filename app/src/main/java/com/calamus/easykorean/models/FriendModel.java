package com.calamus.easykorean.models;

public class FriendModel {
    String phone;
    String name;
    String imageUrl;
    String token;

    public FriendModel(String phone, String name, String imageUrl,String token) {
        this.phone = phone;
        this.name = name;
        this.imageUrl = imageUrl;
        this.token=token;
    }

    public String getToken(){return  token;}

    public String getPhone() {
        return phone;
    }

    public String getName() {
        return name;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
