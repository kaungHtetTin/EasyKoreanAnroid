package com.calamus.easykorean.models;

public class SaveWordModel {
    int id;
    String json;
    String time;

    public SaveWordModel(int id, String json, String time) {
        this.id = id;
        this.json = json;
        this.time=time;
    }

    public String getTime() {
        return time;
    }

    public int getId() {
        return id;
    }

    public String getJson() {
        return json;
    }
}
