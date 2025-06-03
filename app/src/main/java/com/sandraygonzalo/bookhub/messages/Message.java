package com.sandraygonzalo.bookhub.messages;

import com.google.firebase.Timestamp;


public class Message {

    private String senderId;
    private String text;
    private Timestamp timestamp;
    private String type;

    // Constructor vacío requerido por Firebase
    public Message() {
    }

    // Constructor personalizado
    public Message(String senderId, String text, Timestamp timestamp, String type) {
        this.senderId = senderId;
        this.text = text;
        this.timestamp = timestamp;
        this.type = type;
    }

    // Getters
    public String getSenderId() { return senderId; }
    public String getText() { return text; }
    public Timestamp getTimestamp() { return timestamp; }
    public String getType() { return type; }

    // Setters
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setText(String text) { this.text = text; }
    public void setTimestamp(Timestamp timestamp) { this.timestamp = timestamp; }
    public void setType(String type) { this.type = type; }
}





