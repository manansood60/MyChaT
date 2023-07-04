package com.example.mychat;

public class User {
    private String mName;
    private String mEmail;
    private String mUid;

    public User(String name, String email, String uid){
        mName = name;
        mEmail = email;
        mUid = uid;
    }

    public String getmName() {
        return mName;
    }

    public String getmEmail() {
        return mEmail;
    }

    public String getmUid() {
        return mUid;
    }
}
