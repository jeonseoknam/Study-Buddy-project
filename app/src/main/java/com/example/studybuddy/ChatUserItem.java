package com.example.studybuddy;

public class ChatUserItem {
    String name;
    String profileUri;

    public ChatUserItem(String name, String profileUri){
        this.name = name;
        this.profileUri = profileUri;
    }

    public ChatUserItem() {
    }

    public String getName() {
        return name;
    }
    public void setName(String name) {
        this.name = this.name;
    }
    public String getProfileUri() {return profileUri;}
    public void setProfileUri(String profileUri) {
        this.profileUri = profileUri;
    }
}
