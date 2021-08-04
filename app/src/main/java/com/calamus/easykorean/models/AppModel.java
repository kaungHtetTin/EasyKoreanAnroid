package com.calamus.easykorean.models;


public class AppModel {
    final String name;
    final String description;
    final String thumbLink;
    final String appLink;

    public AppModel(String name, String description, String thumbLink, String appLink) {
        this.name = name;
        this.description = description;
        this.thumbLink = thumbLink;
        this.appLink = appLink;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public String getThumbLink() {
        return thumbLink;
    }

    public String getAppLink() {
        return appLink;
    }
}
