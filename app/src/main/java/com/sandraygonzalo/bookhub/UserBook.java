package com.sandraygonzalo.bookhub;

import com.google.firebase.Timestamp;
import java.util.List;

public class UserBook {
    private String title;
    private String author;
    private String coverImage;
    private String condition;
    private String description;
    private List<String> genres;
    private String ownerId;
    private boolean available;
    private Timestamp createdAt;

    public UserBook() {}

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCoverImage() { return coverImage; }
    public String getCondition() { return condition; }
    public String getDescription() { return description; }
    public List<String> getGenres() { return genres; }
    public String getOwnerId() { return ownerId; }
    public boolean isAvailable() { return available; }
    public Timestamp getCreatedAt() { return createdAt; }
}




