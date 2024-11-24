package com.example.studybuddy;

public class StudyTimeItem {
    private String subjectName; // 과목 이름
    private long elapsedTime;   // 공부 시간 (초 단위)

    public StudyTimeItem() {}

    public StudyTimeItem(String subjectName, long elapsedTime) {
        this.subjectName = subjectName;
        this.elapsedTime = elapsedTime;
    }

    public String getSubjectName() {
        return subjectName;
    }

    public void setSubjectName(String subjectName) {
        this.subjectName = subjectName;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
}
