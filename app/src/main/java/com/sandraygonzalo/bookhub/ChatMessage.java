package com.sandraygonzalo.bookhub;

import com.google.firebase.Timestamp;

import com.google.firebase.Timestamp;

public class ChatMessage {
    private String content;
    private String senderId;
    private String type;
    private long sentAt;

    public ChatMessage() {
        // Firestore necesita constructor vacío
    }

    public ChatMessage(String content, String senderId, String type, long sentAt) {
        this.content = content;
        this.senderId = senderId;
        this.type = type;
        this.sentAt = sentAt;
    }

    public String getContent() {
        return content;
    }

    public String getSenderId() {
        return senderId;
    }

    public String getType() {
        return type;
    }

    public long getSentAt() {
        return sentAt;
    }
}
