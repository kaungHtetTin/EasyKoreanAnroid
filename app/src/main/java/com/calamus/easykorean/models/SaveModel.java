package com.calamus.easykorean.models;

public class SaveModel {
    String post_id;
    String post_body;
    String post_image;
    String owner_name;
    String owner_image;
    String isVideo;

    public SaveModel(String post_id, String post_body, String post_image, String owner_name, String owner_image,String isVideo) {
        this.post_id = post_id;
        this.post_body = post_body;
        this.post_image = post_image;
        this.owner_name = owner_name;
        this.owner_image = owner_image;
        this.isVideo=isVideo;
    }

    public String getPost_id() {
        return post_id;
    }

    public String getPost_body() {
        return post_body;
    }

    public String getPost_image() {
        return post_image;
    }

    public String getOwner_name() {
        return owner_name;
    }

    public String getOwner_image() {
        return owner_image;
    }

    public String getIsVideo() {
        return isVideo;
    }

}
