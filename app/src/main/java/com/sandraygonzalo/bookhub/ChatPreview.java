package com.sandraygonzalo.bookhub;

import java.security.Timestamp;
import java.util.List;

public class ChatPreview {
    private String lastMessage;
    private Timestamp lastMessageAt;
    private List<String> participants;

    public ChatPreview() {} // Constructor vacío obligatorio para Firestore

    public String getLastMessage() {
        return lastMessage;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public Timestamp getLastMessageAt() {
        return lastMessageAt;
    }

    public void setLastMessageAt(Timestamp lastMessageAt) {
        this.lastMessageAt = lastMessageAt;
    }

    public List<String> getParticipants() {
        return participants;
    }

    public void setParticipants(List<String> participants) {
        this.participants = participants;
    }
}

