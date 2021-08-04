package com.calamus.easykorean.models;

public class SongOnlineModel {
    String id;
    String title;
    String artist;
    String likeCount;
    String commentCount;
    String downloadCount;
    String url;
    String isLike;
    String drama;

    public SongOnlineModel(){
        this.id = "0";
        this.title = "calamus";
        this.artist = "calamus";
        this.likeCount = "0";
        this.commentCount = "0";
        this.downloadCount = "0";
        this.url = "calamus";
        this.isLike="0";
        this.drama="calamus";
    }

    public SongOnlineModel(String id, String title, String artist, String likeCount, String commentCount, String downloadCount, String url,String isLike,String drama) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.likeCount = likeCount;
        this.commentCount = commentCount;
        this.downloadCount = downloadCount;
        this.url = url;
        this.isLike=isLike;
        this.drama=drama;
    }

    public String getDrama() {
        return drama;
    }

    public String getIsLike() {
        return isLike;
    }

    public void setIsLike(String isLike) {
        this.isLike = isLike;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getLikeCount() {
        return likeCount;
    }

    public String getCommentCount() {
        return commentCount;
    }

    public String getDownloadCount() {
        return downloadCount;
    }

    public String getUrl() {
        return url;
    }

    public void setLikeCount(String likeCount) {
        this.likeCount = likeCount;
    }

    public void setCommentCount(String commentCount) {
        this.commentCount = commentCount;
    }
}
