package com.sandraygonzalo.bookhub;

import com.google.firebase.Timestamp;


public class Message {
    private String content;
    private String senderId;
    private Timestamp sentAt;
    private String type;

    public Message() {}

    public String getContent() { return content; }
    public String getSenderId() { return senderId; }
    public Timestamp getSentAt() { return sentAt; }
    public String getType() { return type; }
}


