package com.example.studybuddy;
public class ChatMessageItem {

    String name;
    String message;
    String time;
    String profileUrl;
    String imageMessage;

    public ChatMessageItem(String name, String message, String time, String profileUrl, String imageMessage) {
        this.name = name;
        this.message = message;
        this.time = time;
        this.profileUrl = profileUrl;
        this.imageMessage = imageMessage;
    }
    public ChatMessageItem() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getImageMessage() {
        return imageMessage;
    }

    public void setImageMessage(String imageMessage) {
        this.imageMessage = imageMessage;
    }
}
