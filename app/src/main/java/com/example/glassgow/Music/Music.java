package com.example.glassgow.Music;

public class Music {

    private String name;
    private String artist;
    private String duration;
    private String type;
    private int millisUntilFinished;
    private int timeInMillis;
    private int minutes;
    private int secondes;

    public Music(String name, String artist, String duration, String type) {
        this.name = name;
        this.artist = artist;
        this.duration = duration;
        this.type = type;
        this.timeInMillis = 0;
        this.millisUntilFinished = 0;
        this.minutes = 0;
        this.secondes = 0;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getArtist() {
        return artist;
    }

    public void setArtist(String artist) {
        this.artist = artist;
    }

    public String getDuration() {
        return duration;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public int getMillisUntilFinished() {
        return millisUntilFinished;
    }

    public void setMillisUntilFinished(int millisUntilFinished) {
        this.millisUntilFinished = millisUntilFinished;
    }

    public int getTimeInMillis() {
        return timeInMillis;
    }

    public void setTimeInMillis(int timeInMillis) {
        this.timeInMillis = timeInMillis;
    }

    public int getMinutes() {
        return minutes;
    }

    public void setMinutes(int minutes) {
        this.minutes = minutes;
    }

    public int getSecondes() {
        return secondes;
    }

    public void setSecondes(int secondes) {
        this.secondes = secondes;
    }
}
