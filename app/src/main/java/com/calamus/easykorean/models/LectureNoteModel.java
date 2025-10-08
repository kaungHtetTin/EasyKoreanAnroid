package com.calamus.easykorean.models;

public class LectureNoteModel {
    String time;
    String note;

    public LectureNoteModel(String time, String note) {
        this.time = time;
        this.note = note;
    }

    public String getNote() {
        return note;
    }

    public String getTime() {
        return time;
    }
}
