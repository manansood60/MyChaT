package com.example.mychat;

public class User {
    private String name;
    private String email;
    private String uid;

    public User(){}
    public User(String name, String email, String uid){
        this.name = name;
        this.email = email;
        this.uid = uid;
    }

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
}
