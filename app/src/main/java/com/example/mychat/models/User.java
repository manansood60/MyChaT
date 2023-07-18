package com.example.mychat.models;

public class User {
    private String name;
    private String email;
    private String uid;
    private String profilePicture;

    public User(){}

    public User(String name, String email, String uid, String profilePicture) {
        this.name = name;
        this.email = email;
        this.uid = uid;
        this.profilePicture = profilePicture;
    }



    public String getProfilePicture() { return profilePicture; }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getUid() {
        return uid;
    }

    public void setName(String name) { this.name = name; }

    public void setEmail(String email) { this.email = email; }

    public void setUid(String uid) { this.uid = uid; }

    public void setProfilePicture(String profilePicture) { this.profilePicture = profilePicture; }
}
