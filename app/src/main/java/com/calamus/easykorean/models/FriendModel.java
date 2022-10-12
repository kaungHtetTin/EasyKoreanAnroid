package com.calamus.easykorean.models;


public class FriendModel extends StudentModel {

    public FriendModel(){
        super();
    }

    public FriendModel(String phone, String name, String imageUrl,String token) {
        super(phone, name, imageUrl, token);
    }


    public FriendModel(String phone, String name, String imageUrl, String token, String friendsJson) {
        super(phone, name, imageUrl, token, friendsJson);
    }

}
