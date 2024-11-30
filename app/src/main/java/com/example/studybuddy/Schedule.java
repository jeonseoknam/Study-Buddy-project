package com.example.studybuddy;

import java.io.Serializable;

public class Schedule implements Serializable {
    private String date;
    private String description;

    public Schedule(String date, String description) {
        this.date = date;
        this.description = description;
    }

    public String getDate() {
        return date;
    }

    public String getDescription() {
        return description;
    }
}
