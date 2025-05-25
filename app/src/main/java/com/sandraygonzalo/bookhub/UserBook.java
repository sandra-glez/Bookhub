package com.sandraygonzalo.bookhub;

import java.security.Timestamp;

public class UserBook {
    private String ownerId;
    private String generalBookId;
    private boolean available;
    private String condition;
    private String notes;

    // ✅ Campos que necesitas para mostrar en la interfaz:
    private String title;
    private String author;
    private String coverImage;

    public UserBook() {
        // Constructor vacío requerido por Firestore
    }

    public String getOwnerId() { return ownerId; }
    public String getGeneralBookId() { return generalBookId; }
    public boolean isAvailable() { return available; }
    public String getCondition() { return condition; }
    public String getNotes() { return notes; }

    public String getTitle() { return title; }
    public String getAuthor() { return author; }
    public String getCoverImage() { return coverImage; }
}




