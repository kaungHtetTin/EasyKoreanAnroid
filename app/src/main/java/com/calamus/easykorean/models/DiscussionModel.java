package com.calamus.easykorean.models;

public class DiscussionModel {
    String userId;
    String userName;
    String userImage;
    String postId;
    String body;
    String postLikes;
    String comments;
    String videoCount;
    String hasVideo;
    String postImage;

    public DiscussionModel(String userId,String userName, String userImage, String postId, String body, String postLikes, String comments, String videoCount, String hasVideo,String postImage) {
        this.userId=userId;
        this.userName = userName;
        this.userImage = userImage;
        this.postId = postId;
        this.body = body;
        this.postLikes = postLikes;
        this.comments = comments;
        this.videoCount = videoCount;
        this.hasVideo = hasVideo;
        this.postImage=postImage;
    }

    public String getUserId() {
        return userId;
    }

    public String getPostImage() {
        return postImage;
    }

    public String getUserName() {
        return userName;
    }

    public String getUserImage() {
        return userImage;
    }

    public String getPostId() {
        return postId;
    }

    public String getBody() {
        return body;
    }

    public String getPostLikes() {
        return postLikes;
    }

    public String getComments() {
        return comments;
    }

    public String getVideoCount() {
        return videoCount;
    }

    public String getHasVideo() {
        return hasVideo;
    }
}
