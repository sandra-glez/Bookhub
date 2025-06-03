package com.sandraygonzalo.bookhub.messages;

import com.google.firebase.Timestamp;


public class Message {

    private String senderId;
    private String content;
    private Timestamp sentAt;
    private String type;

    // Constructor vacío requerido por Firebase
    public Message() {
    }

    // Constructor personalizado
    public Message(String senderId, String content, Timestamp sentAt, String type) {
        this.senderId = senderId;
        this.content = content;
        this.sentAt = sentAt;
        this.type = type;
    }

    // Getters (Firebase los necesita para mapear los datos)
    public String getSenderId() { return senderId; }
    public String getContent() { return content; }
    public Timestamp getSentAt() { return sentAt; }
    public String getType() { return type; }

    // Opcional: Setters (si necesitas actualizar valores)
    public void setSenderId(String senderId) { this.senderId = senderId; }
    public void setContent(String content) { this.content = content; }
    public void setSentAt(Timestamp sentAt) { this.sentAt = sentAt; }
    public void setType(String type) { this.type = type; }
}



