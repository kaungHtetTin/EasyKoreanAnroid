package com.calamus.easykorean.models;

public class NotiModel {

    String writerName;
    String writerImage;
    String postId;
    String postBody;
    String seen;
    String time;
    String isVip;
    String action;
    String has_video;
    String postImage;
    String color;
    String comment_id;

    public NotiModel(String writerName, String writerImage, String postId, String postBody, String seen,String time,String isVip,String action,String has_video,String postImage,String color,String comment_id) {
        this.writerName = writerName;
        this.isVip=isVip;
        this.writerImage = writerImage;
        this.postId = postId;
        this.postBody = postBody;
        this.time=time;
        this.seen = seen;
        this.action=action;
        this.has_video=has_video;
        this.postImage=postImage;
        this.color=color;
        this.comment_id=comment_id;
    }

    public String getComment_id() {
        return comment_id;
    }

    public String getColor() {
        return color;
    }

    public String getWriterName() {
        return writerName;
    }

    public String getWriterImage() {
        return writerImage;
    }

    public String getPostId() {
        return postId;
    }

    public String getPostBody() {
        return postBody;
    }

    public String getSeen() {
        return seen;
    }

    public String getTime() {
        return time;
    }

    public String getIsVip() {
        return isVip;
    }


    public String getAction() {
        return action;
    }

    public String getHas_video() {
        return has_video;
    }

    public String getPostImage() {
        return postImage;
    }

}
