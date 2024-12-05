package com.example.studybuddy;

public class ChatTimerRankingItem {
    private final String nickname;
    private final long totalTime;

    public ChatTimerRankingItem(String nickname, long totalTime) {
        this.nickname = nickname;
        this.totalTime = totalTime;
    }

    public String getNickname() {
        return nickname;
    }

    public long getTotalTime() {
        return totalTime;
    }
}
