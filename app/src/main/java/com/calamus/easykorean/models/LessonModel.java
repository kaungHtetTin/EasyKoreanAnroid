package com.calamus.easykorean.models;


import java.io.File;

public class LessonModel  {
    String id;
    String link;
    String title;
    String title_mini;
    boolean isVideo;
    boolean isVip;
    long time;
    boolean learned;
    String image_url;
    String thumbnail;
    int duration;
    String category;
    boolean downloaded;
    SavedVideoModel model;

    public LessonModel(String id, String link, String title,String title_mini, boolean isVideo,
                       boolean isVip,long time,  boolean learned,String image_url,String thumbnail,
                       int duration,String category) {
        this.id=id;
        this.link = link;
        this.title = title;
        this.title_mini=title_mini;
        this.isVideo = isVideo;
        this.isVip=isVip;
        this.time=time;
        this.learned=learned;
        this.image_url=image_url;
        this.thumbnail=thumbnail;
        this.duration=duration;
        this.category=category;
    }

    public String getCategory() {
        return category;
    }

    public int getDuration() {
        return duration;
    }

    public String getTitle_mini() {
        return title_mini;
    }

    public String getId() {
        return id;
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

    public boolean isLearned() {
        return learned;
    }

    public void setLearned(boolean learned) {
        this.learned = learned;
    }

    public String getImage_url() {
        return image_url;
    }

    public String getThumbnail() {
        return thumbnail;
    }

    public void setDownloaded(boolean downloaded) {
        this.downloaded=downloaded;
    }

    public boolean isDownloaded() {
        return downloaded;
    }

    public SavedVideoModel getVideoModel() {
        return model;
    }

    public void setVideoModel(SavedVideoModel model) {
        this.model = model;
    }
}
