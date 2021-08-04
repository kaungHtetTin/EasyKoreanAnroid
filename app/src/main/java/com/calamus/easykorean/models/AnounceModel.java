package com.calamus.easykorean.models;

public class AnounceModel {
    String linkAounce;
    String isSeen;

    public AnounceModel(String linkAounce, String isSeen) {
        this.linkAounce = linkAounce;
        this.isSeen = isSeen;
    }

    public String getlinkAounce() {
        return linkAounce;
    }

    public String getIsSeen() {
        return isSeen;
    }
}
