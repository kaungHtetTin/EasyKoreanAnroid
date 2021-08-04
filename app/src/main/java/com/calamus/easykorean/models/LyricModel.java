package com.calamus.easykorean.models;

public class LyricModel {
    String time;
    String korea;
    String myanmar;

    public LyricModel(String time, String korea, String myanmar) {
        this.time = time;
        this.korea = korea;
        this.myanmar = myanmar;
    }

    public String getTime() {
        return time;
    }

    public String getKorea() {
        return korea;
    }

    public String getMyanmar() {
        return myanmar;
    }
}
