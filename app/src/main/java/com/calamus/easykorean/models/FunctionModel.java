package com.calamus.easykorean.models;

public class FunctionModel {
    String name;
    String link;
    String url;

    public FunctionModel(String name, String link, String url) {
        this.name = name;
        this.link = link;
        this.url = url;
    }

    public String getName() {
        return name;
    }

    public String getLink() {
        return link;
    }

    public String getUrl() {
        return url;
    }
}
