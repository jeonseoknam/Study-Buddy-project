package com.example.studybuddy;

public class StudySession {
    private String id;
    private String user_id; // Firestore의 필드 이름과 일치
    private String elapsed_time; // Firestore의 필드 이름과 일치
    private String subject_name; // Firestore의 필드 이름과 일치
    private String memo;

    public StudySession() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getUser_id() {
        return user_id;
    }

    public void setUser_id(String user_id) {
        this.user_id = user_id;
    }

    public String getElapsed_time() {
        return elapsed_time;
    }

    public void setElapsed_time(String elapsed_time) {
        this.elapsed_time = elapsed_time;
    }

    public String getSubject_name() {
        return subject_name;
    }

    public void setSubject_name(String subject_name) {
        this.subject_name = subject_name;
    }

    public String getMemo() {
        return memo;
    }

    public void setMemo(String memo) {
        this.memo = memo;
    }
}
