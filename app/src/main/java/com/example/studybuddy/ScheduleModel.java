package com.example.studybuddy;

public class ScheduleModel {
    private String title;
    private String date;
    private String time;
    private String name;
    private String profileUrl;

    public ScheduleModel(String title, String date, String time, String name, String profileUrl) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.name = name;
        this.profileUrl = profileUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getName() {
        return name;
    }

    public String getProfileUrl() {
        return profileUrl;
    }
}
