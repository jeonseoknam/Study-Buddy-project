package com.example.studybuddy;

public class ChatListItem {

    String chatname;
    String message;
    String time;
    String profileUrl;

    public ChatListItem(String chatname, String message, String time, String profileUrl) {
        this.chatname = chatname;
        this.message = message;
        this.time = time;
        this.profileUrl = profileUrl;
    }
    public ChatListItem() {
    }

    public String getName() {
        return chatname;
    }

    public void setName(String chatname) {
        this.chatname = chatname;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getProfileUrl() {
        return profileUrl;
    }

    public void setProfileUrl(String profileUrl) {
        this.profileUrl = profileUrl;
    }
}
