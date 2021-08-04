package com.calamus.easykorean.models;

public class GameWordModel {
    String displayWord;
    String displayAudio;
    String displayImage;
    String category;
    String ansA;
    String ansB;
    String ansC;

    public GameWordModel(String displayWord, String displayAudio, String displayImage, String category, String ansA, String ansB, String ansC) {
        this.displayWord = displayWord;
        this.displayAudio = displayAudio;
        this.displayImage = displayImage;
        this.category = category;
        this.ansA = ansA;
        this.ansB = ansB;
        this.ansC = ansC;
    }

    public String getDisplayWord() {
        return displayWord;
    }

    public String getDisplayAudio() {
        return displayAudio;
    }

    public String getDisplayImage() {
        return displayImage;
    }

    public String getCategory() {
        return category;
    }

    public String getAnsA() {
        return ansA;
    }

    public String getAnsB() {
        return ansB;
    }

    public String getAnsC() {
        return ansC;
    }
}
