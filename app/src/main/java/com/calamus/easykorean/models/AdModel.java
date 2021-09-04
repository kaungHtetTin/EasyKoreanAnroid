package com.calamus.easykorean.models;

public class AdModel {
    String id;
    String appName;
    String appDes;
    String link;
    String appCover;
    String appIcon;
    String type;

    public AdModel(String id, String appName, String appDes, String link, String appCover, String appIcon, String type) {
        this.id = id;
        this.appName = appName;
        this.appDes = appDes;
        this.link = link;
        this.appCover = appCover;
        this.appIcon = appIcon;
        this.type=type;
    }

    public String getType() {
        return type;
    }

    public String getId() {
        return id;
    }

    public String getAppName() {
        return appName;
    }

    public String getAppDes() {
        return appDes;
    }

    public String getLink() {
        return link;
    }

    public String getAppCover() {
        return appCover;
    }

    public String getAppIcon() {
        return appIcon;
    }
}
