package com.example.studybuddy;


public class ChatSchedule {
    private String title;
    private String date;
    private String time;
    private String description;

    // 기본 생성자 (Firestore 데이터 매핑용)
    public ChatSchedule() {}

    // 생성자
    public ChatSchedule(String title, String date, String time, String description) {
        this.title = title;
        this.date = date;
        this.time = time;
        this.description = description;
    }

    // Getter 메서드
    public String getTitle() {
        return title;
    }

    public String getDate() {
        return date;
    }

    public String getTime() {
        return time;
    }

    public String getDescription() {
        return description;
    }

    // Setter 메서드
    public void setTitle(String title) {
        this.title = title;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
