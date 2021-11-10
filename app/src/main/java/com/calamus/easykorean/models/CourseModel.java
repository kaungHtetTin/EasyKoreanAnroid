package com.calamus.easykorean.models;

public class CourseModel {
    String id;
    String title;
    String subject;
    int enrollProgress;

    public CourseModel(String id, String title, String subject, int enrollProgress) {
        this.id = id;
        this.title = title;
        this.subject = subject;
        this.enrollProgress = enrollProgress;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getSubject() {
        return subject;
    }

    public int getEnrollProgress() {
        return enrollProgress;
    }
}
