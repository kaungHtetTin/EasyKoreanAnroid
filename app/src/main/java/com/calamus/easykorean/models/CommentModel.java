package com.calamus.easykorean.models;

public class CommentModel {
    String postId;
    String imageUrl;
    String name;
    String comment;
    String time;
    String isVip;
    String writerId;
    String writerToken;
    String likes;
    String isLiked;
    String commentImage;

    public CommentModel(String postId,String imageUrl, String name, String comment, String time,String isVip,String writerId,String writerToken,String likes,String isLiked,String commentImage) {
        this.imageUrl = imageUrl;
        this.name = name;
        this.comment = comment;
        this.time = time;
        this.isVip=isVip;
        this.writerId=writerId;
        this.writerToken=writerToken;
        this.postId=postId;
        this.likes=likes;
        this.isLiked=isLiked;
        this.commentImage=commentImage;
    }

    public String getLikes() {
        return likes;
    }


    public void setLikes(String likes) {
        this.likes = likes;
    }

    public String getIsLiked() {
        return isLiked;
    }

    public void setIsLiked(String isLiked) {
        this.isLiked = isLiked;
    }

    public String getPostId() {
        return postId;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getIsVip() {
        return isVip;
    }

    public void setIsVip(String isVip) {
        this.isVip = isVip;
    }

    public String getWriterId() {
        return writerId;
    }

    public void setWriterId(String writerId) {
        this.writerId = writerId;
    }

    public String getWriterToken() {
        return writerToken;
    }

    public void setWriterToken(String writerToken) {
        this.writerToken = writerToken;
    }

    public String getCommentImage() {
        return commentImage;
    }
}
