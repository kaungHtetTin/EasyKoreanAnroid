package com.calamus.easykorean.models;

public class NewfeedModel {
    String userName;
    String userId;
    String userToken;
    String userImage;
    String postId;
    String postBody;
    String postLikes;
    String postComments;
    String postImage;
    String isVip;
    String isVideo;
    String viewCount;
    String isLike;
    int shareCount;
    long share;
    public NewfeedModel(String userName, String userId, String userToken, String userImage, String postId, String postBody, String postLikes, String postComments, String postImage,String isVip,String isVideo, String viewCount,String isLike,int shareCount,long share) {
        this.userName = userName;
        this.userId = userId;
        this.userToken = userToken;
        this.userImage = userImage;
        this.postId = postId;
        this.postBody = postBody;
        this.postLikes = postLikes;
        this.postComments = postComments;
        this.postImage = postImage;
        this.isVip=isVip;
        this.isVideo=isVideo;
        this.viewCount=viewCount;
        this.isLike=isLike;
        this.share=share;
        this.shareCount=shareCount;
    }

    public int getShareCount() {
        return shareCount;
    }

    public long getShare() {
        return share;
    }

    public String getIsLike() {
        return isLike;
    }

    public void setIsLike(String isLike) {
        this.isLike = isLike;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserToken() {
        return userToken;
    }

    public void setUserToken(String userToken) {
        this.userToken = userToken;
    }

    public String getUserImage() {
        return userImage;
    }

    public void setUserImage(String userImage) {
        this.userImage = userImage;
    }

    public String getPostId() {
        return postId;
    }

    public void setPostId(String postId) {
        this.postId = postId;
    }

    public String getPostBody() {
        return postBody;
    }

    public void setPostBody(String postBody) {
        this.postBody = postBody;
    }

    public String getPostLikes() {
        return postLikes;
    }

    public void setPostLikes(String postLikes) {
        this.postLikes = postLikes;
    }

    public String getPostComments() {
        return postComments;
    }

    public void setPostComments(String postComments) {
        this.postComments = postComments;
    }

    public String getPostImage() {
        return postImage;
    }

    public void setPostImage(String postImage) {
        this.postImage = postImage;
    }

    public String getIsVip() {
        return isVip;
    }

    public void setIsVip(String isVip) {
        this.isVip = isVip;
    }

    public String getIsVideo() {
        return isVideo;
    }

    public void setIsVideo(String isVideo) {
        this.isVideo = isVideo;
    }

    public String getViewCount() {
        return viewCount;
    }
}

