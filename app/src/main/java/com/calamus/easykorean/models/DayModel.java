package com.calamus.easykorean.models;

import org.json.JSONArray;

public class DayModel {
    String day;
    String course_id;
    JSONArray jsonArray;

    public DayModel(String day, String course_id,JSONArray jsonArray) {
        this.day = day;
        this.course_id = course_id;
        this.jsonArray=jsonArray;
    }

    public String getDay() {
        return day;
    }

    public String getCourse_id() {
        return course_id;
    }

    public JSONArray getJsonArray() {
        return jsonArray;
    }
}
