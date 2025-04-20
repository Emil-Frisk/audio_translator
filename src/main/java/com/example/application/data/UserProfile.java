package com.example.application.data;

import java.time.LocalDateTime;

public class UserProfile {
    public int userId;
    public String preferredTargetLanguage;
    public String preferredOriginLanguage;
    public LocalDateTime createdAt;
    
    public UserProfile(int userId, String preferredTargetLanguage, String preferredOriginLanguage,
            LocalDateTime createdAt) {
        this.userId = userId;
        this.preferredTargetLanguage = preferredTargetLanguage;
        this.preferredOriginLanguage = preferredOriginLanguage;
        this.createdAt = createdAt;
    }
    public int getUserId() {
        return userId;
    }
    public void setUserId(int userId) {
        this.userId = userId;
    }
    public String getPreferredTargetLanguage() {
        return preferredTargetLanguage;
    }
    public void setPreferredTargetLanguage(String preferredTargetLanguage) {
        this.preferredTargetLanguage = preferredTargetLanguage;
    }
    public String getPreferredOriginLanguage() {
        return preferredOriginLanguage;
    }
    public void setPreferredOriginLanguage(String preferredOriginLanguage) {
        this.preferredOriginLanguage = preferredOriginLanguage;
    }
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }
}
