package com.calamus.easykorean.models;

public class DayItemModel {
    String title;
    boolean isLearned;

    public DayItemModel(String title, boolean isLearned) {
        this.title = title;
        this.isLearned = isLearned;
    }

    public String getTitle() {
        return title;
    }

    public boolean isLearned() {
        return isLearned;
    }
}
