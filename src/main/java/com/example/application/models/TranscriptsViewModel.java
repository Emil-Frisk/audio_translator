package com.example.application.models;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import com.example.application.data.TranslatedTranscript;

public class TranscriptsViewModel {
    private String text_language;
    private String text_name;
    private String uuid;

   
    public TranscriptsViewModel(String text_language, String text_name, LocalDateTime created_at, String uuid) {
        this.text_language = text_language;
        this.text_name = text_name;
        this.created_at = created_at;
        this.uuid = uuid;
    }

    public TranscriptsViewModel(TranslatedTranscript transcript) {
        this(
            transcript.getText_language(),
            transcript.getText_name(),
            transcript.getCreated_at(),
            transcript.getUuid()
        );
    }

    public String getUuid() {
        return uuid;
    }
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }
    private LocalDateTime created_at;

    public String getText_language() {
        return text_language;
    }
    public void setText_language(String text_language) {
        this.text_language = text_language;
    }
    public String getText_name() {
        return text_name;
    }
    public void setText_name(String text_name) {
        this.text_name = text_name;
    }
    
    public String getCreated_at() {

        return created_at.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
    }
    public void setCreated_at(LocalDateTime created_at) {
        this.created_at = created_at;
    }
}


