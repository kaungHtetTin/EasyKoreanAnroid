package com.calamus.easykorean.models;

public class EnrollModel {
    String course_id;
    int learned;
    int total;

    public EnrollModel(String course_id, int learned, int total) {
        this.course_id = course_id;
        this.learned = learned;
        this.total = total;
    }

    public String getCourse_id() {
        return course_id;
    }

    public int getLearned() {
        return learned;
    }

    public int getTotal() {
        return total;
    }
}
