package com.calamus.easykorean.models;

public class SongArtistModel {
    String artist;
    String url;

    public SongArtistModel(String artist, String url) {
        this.artist = artist;
        this.url = url;
    }

    public String getArtist() {
        return artist;
    }

    public String getUrl() {
        return "https://www.calamuseducation.com/uploads/songs/image/"+url+".png";
    }
}
