package com.sandraygonzalo.bookhub;


import com.google.firebase.Timestamp;

import java.util.List;

public class ChatPreview {
    private String exchangeId;
    private String username;
    private String lastMessage;
    private Timestamp lastMessageAt;
    private String bookCoverUrl;

    public ChatPreview() {
    }

    public ChatPreview(String exchangeId, String username, String lastMessage, Timestamp lastMessageAt, String bookCoverUrl) {
        this.exchangeId = exchangeId;
        this.username = username;
        this.lastMessage = lastMessage;
        this.lastMessageAt = lastMessageAt;
        this.bookCoverUrl = bookCoverUrl;
    }

    public String getExchangeId() {
        return exchangeId;
    }

    public String getUsername() {
        return username;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public Timestamp getLastMessageAt() {
        return lastMessageAt;
    }

    public String getBookCoverUrl() {
        return bookCoverUrl;
    }
}

