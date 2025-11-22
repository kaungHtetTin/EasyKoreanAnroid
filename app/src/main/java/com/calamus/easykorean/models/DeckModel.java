package com.calamus.easykorean.models;

public class DeckModel {
    String id;
    String title;
    int newWord;
    int recallWord;
    int progress;
    int totalWord;
    int masteredWord;
    int learnedWord;

    public DeckModel(String id, int learnedWord, int masteredWord, int totalWord, int progress, int recallWord,
                     int newWord, String title) {
        this.id = id;
        this.masteredWord = masteredWord;
        this.totalWord = totalWord;
        this.progress = progress;
        this.recallWord = recallWord;
        this.newWord = newWord;
        this.title = title;
        this.learnedWord = learnedWord;
    }

    public String getId() {
        return id;
    }

    public int getLearnedWord() {
        return learnedWord;
    }

    public int getMasteredWord() {
        return masteredWord;
    }

    public int getTotalWord() {
        return totalWord;
    }

    public int getProgress() {
        return progress;
    }

    public int getRecallWord() {
        return recallWord;
    }

    public int getNewWord() {
        return newWord;
    }


    public String getTitle() {
        return title;
    }
}
