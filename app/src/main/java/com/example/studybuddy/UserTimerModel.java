
package com.example.studybuddy;

public class UserTimerModel {
    private String userName;
    private String profileImage;
    private long elapsedTime;

    public UserTimerModel(String userName, String profileImage, long elapsedTime) {
        this.userName = userName;
        this.profileImage = profileImage;
        this.elapsedTime = elapsedTime;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public long getElapsedTime() {
        return elapsedTime;
    }

    public void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
}
