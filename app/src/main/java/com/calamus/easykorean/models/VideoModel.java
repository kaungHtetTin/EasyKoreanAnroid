package com.calamus.easykorean.models;

public class VideoModel {
    String videoTitle;
    String videoId;
    long time;
    String category;
    boolean learned;

    public VideoModel(String videoTitle,String videoId,long time,String category,boolean learned){
        this.videoTitle=videoTitle;
        this.videoId=videoId;
        this.time=time;
        this.category=category;
        this.learned=learned;
    }

    public String getVideoTitle()
    {
        return videoTitle;
    }

    public String getVideoId(){
        return videoId;
    }

    public long getTime(){
        return time;
    }

    public String getCategory(){
        return category;
    }

    public boolean isLearned() {
        return learned;
    }

    public void setLearned(boolean learned) {
        this.learned = learned;
    }
}