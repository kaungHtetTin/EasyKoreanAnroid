package com.calamus.easykorean.models;

public class VideoModel {
    String videoTitle="";
    String videoId="";
    long time=0;
    String category="";

    public VideoModel(String videoTitle,String videoId,long time,String category){
        this.videoTitle=videoTitle;
        this.videoId=videoId;
        this.time=time;
        this.category=category;
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

}