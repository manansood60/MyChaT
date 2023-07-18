package com.example.mychat.models;

public class Message {

    private String message;
    private String senderId;
    private String messageId;
    private int feeling = -1;  // setting default value to no feeling.
    private long time;

    public Message(){}

    public Message(String message, String senderId, long time) {
        this.message = message;
        this.senderId = senderId;
        this.time = time;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSenderId(String senderId) {
        this.senderId = senderId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public int getFeeling() {
        return feeling;
    }

    public void setFeeling(int feeling) {
        this.feeling = feeling;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public String getMessage(){
        return message;
    }
    public String getSenderId(){
        return senderId;
    }
}
