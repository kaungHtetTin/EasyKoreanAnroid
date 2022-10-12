package com.calamus.easykorean.models;

public class FinishCourseModel {
    String title;
    int learned;
    int total;

    public FinishCourseModel(String title, int learned, int total) {
        this.title = title;
        this.learned = learned;
        this.total = total;
    }

    public String getTitle() {
        return title;
    }

    public int getLearned() {
        return learned;
    }

    public int getTotal() {
        return total;
    }
}
