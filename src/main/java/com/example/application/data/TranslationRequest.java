package com.example.application.data;

import java.io.File;

public class TranslationRequest {
    private File audioFile;
    private String targetLanguage;

    // Getters and setters
    public File getAudioFile() {
        return audioFile;
    }

    public void setAudioFile(File audioFile) {
        this.audioFile = audioFile;
    }

    public String getTargetLanguage() {
        return targetLanguage;
    }

    public void setTargetLanguage(String targetLanguage) {
        this.targetLanguage = targetLanguage;
    }
}
