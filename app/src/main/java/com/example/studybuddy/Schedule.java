package com.example.studybuddy;

import java.io.Serializable;

public class Schedule implements Serializable {
    private String date;
    private String description;
    private String time;

    public Schedule(String date, String description, String time) {
        this.date = date;
        this.description = description;
        this.time = time;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }

    public String getTime() {
        return time;
    }
}
