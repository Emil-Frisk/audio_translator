package com.example.application.user;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import com.example.application.data.TranslatedTranscript;

@Repository
public class TranscriptRepository {
    private final JdbcClient jdbcClient;

    public TranscriptRepository(JdbcClient jdbcClient) {
        this.jdbcClient = jdbcClient;
    }

    public List<TranslatedTranscript> findAll() {
        return jdbcClient.sql("SELECT * FROM translated_transcript")
                .query(TranslatedTranscript.class)
                .list();
    }

    public List<TranslatedTranscript> getUsersTranscripts(int userId) {
        return jdbcClient.sql("SELECT * FROM translated_transcript WHERE user_id = ?")
                .param(userId)
                .query(TranslatedTranscript.class)
                .list();
    }

    public void create(TranslatedTranscript transcript) {
        var updated = jdbcClient.sql("INSERT INTO translated_transcript(user_id, text_language, uuid, text_name, created_at) VALUES (?, ?, ?, ?, ?)")
            .params(List.of(transcript.getUserId(), transcript.getText_language(), transcript.getUuid(), transcript.getText_name(), transcript.getCreated_at()))
            .update();

        Assert.state(updated == 1, "Failed to create translated transcript " + transcript.getText_name());
        System.out.println("successfully made translated transcript entry");
    }
}

