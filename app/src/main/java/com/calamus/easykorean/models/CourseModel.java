package com.calamus.easykorean.models;

public class CourseModel {
    String course_id;
    String title;
    String description;
    String cover_url;
    int lesson_count;
    int duration;
    boolean isVip;
    String colorCode;

    public CourseModel(String course_id, String title, String description, String cover_url,String colorCode, int lesson_count, int duration, boolean isVip) {
        this.course_id = course_id;
        this.title = title;
        this.description = description;
        this.cover_url = cover_url;
        this.lesson_count = lesson_count;
        this.isVip = isVip;
        this.duration=duration;
        this.colorCode=colorCode;
    }

    public String getColorCode() {
        return colorCode;
    }

    public String getCourse_id() {
        return course_id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public String getCover_url() {
        return cover_url;
    }

    public int getLesson_count() {
        return lesson_count;
    }

    public boolean isVip() {
        return isVip;
    }

    public int getDuration() {
        return duration;
    }
}
