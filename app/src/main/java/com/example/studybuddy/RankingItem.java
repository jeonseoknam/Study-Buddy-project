package com.example.studybuddy;

public class RankingItem {
    private String userId;
    private long totalTime;

    public RankingItem(String userId, long totalTime) {
        this.userId = userId;
        this.totalTime = totalTime;
    }

    public String getUserId() {
        return userId;
    }

    public long getTotalTime() {
        return totalTime;
    }
}
