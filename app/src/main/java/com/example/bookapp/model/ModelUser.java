package com.example.bookapp.model;

public class ModelUser {
    private String name;
    private String email;
    private String profileImage;
    private long timestamp;
    private String uid;
    private String userType;

    public ModelUser() {
    }

    public ModelUser(String name, String email, String profileImage, long timestamp, String uid, String userType) {
        this.name = name;
        this.email = email;
        this.profileImage = profileImage;
        this.timestamp = timestamp;
        this.uid = uid;
        this.userType = userType;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getProfileImage() {
        return profileImage;
    }

    public void setProfileImage(String profileImage) {
        this.profileImage = profileImage;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }
}
