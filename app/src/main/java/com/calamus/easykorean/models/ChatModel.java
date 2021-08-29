package com.calamus.easykorean.models;

public class ChatModel {
    final String senderId;
    final String msg;
    final long time;
    final int seen;
    final String imageUrl;

    public ChatModel(String senderId, String msg, long time,int seen,String imageUrl) {
        this.senderId= senderId;
        this.msg = msg;
        this.time = time;
        this.seen=seen;
        this.imageUrl=imageUrl;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getMsg() {
        return msg;
    }

    public long getTime() {
        return time;
    }

    public int getSeen() {
        return seen;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
