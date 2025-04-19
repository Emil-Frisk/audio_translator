package com.example.application.data;

import java.time.LocalDateTime;

public class TranslatedTranscript {
    private int Id;
    private int userId;

    public LocalDateTime getCreated_at() {
        return created_at;
    }

    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
    private String text_language;
    private String uuid;
    private String text_name;
    private LocalDateTime created_at;
    
    public TranslatedTranscript(String text_language, String uuid, String text_name, int userId) {
        this.text_language = text_language;
        this.uuid = uuid;
        this.text_name = text_name;
        this.userId = userId;
        this.created_at = LocalDateTime.now();
    }

    public int getId() {
        return Id;
    }
    public void setId(int id) {
        Id = id;
    }

    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getText_language() {
        return text_language;
    }
    public void setText_language(String text_language) {
        this.text_language = text_language;
    }
    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    public String getText_name() {
        return text_name;
    }
    public void setText_name(String text_name) {
        this.text_name = text_name;
    }
}
