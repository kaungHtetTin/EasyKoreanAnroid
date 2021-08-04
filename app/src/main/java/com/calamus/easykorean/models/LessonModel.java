package com.calamus.easykorean.models;


public class LessonModel  {
    String cate;
    String link;
    String title;
    boolean isVideo;
    boolean isVip;
    long time;

    public LessonModel(String cate, String link, String title, boolean isVideo, boolean isVip,long time) {
        this.cate = cate;
        this.link = link;
        this.title = title;
        this.isVideo = isVideo;
        this.isVip=isVip;
        this.time=time;
    }

    public String getCate() {
        return cate;
    }

    public void setCate(String cate) {
        this.cate = cate;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public boolean isVideo() {
        return isVideo;
    }

    public void setVideo(boolean video) {
        isVideo = video;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public boolean isVip() {
        return isVip;
    }

    public void setVip(boolean vip) {
        isVip = vip;
    }
}
