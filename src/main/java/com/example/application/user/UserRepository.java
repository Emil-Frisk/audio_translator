package com.example.application.user;

import java.sql.PreparedStatement;
import java.util.List;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.example.application.data.User;

@Repository
public class UserRepository {
    private final JdbcClient jdbcClient;
    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder;

    public UserRepository(JdbcClient jdbcClient, JdbcTemplate jdbcTemplate) {
        this.jdbcClient = jdbcClient;
        this .jdbcTemplate = jdbcTemplate;
        this.passwordEncoder = new BCryptPasswordEncoder();
        createTranslatedAudioTable();
        createUserTable();
        createUserAudioTable();
        createAdminUser();
    }

    private void createTranslatedAudioTable() {
        jdbcClient.sql("""
                    CREATE TABLE IF NOT EXISTS translated_audio (
                    id INT NOT NULL AUTO_INCREMENT,
                    duration INT NOT NULL,
                    audio_language VARCHAR(250),
                    audio_name VARCHAR(250) NOT NULL UNIQUE,
                    PRIMARY KEY (id)
                    )
                    """).update();
    }

    private void createUserTable() {
        jdbcClient.sql("""
            CREATE TABLE IF NOT EXISTS app_user (
                id INT NOT NULL AUTO_INCREMENT,
                email VARCHAR(250) NOT NULL,
                user_password VARCHAR(250) NOT NULL,
                user_role VARCHAR(250) NOT NULL,
                PRIMARY KEY (id)
            )
            """).update();
    }

    private void createUserAudioTable() {
        jdbcClient.sql("""
                    CREATE TABLE IF NOT EXISTS user_translated_audio (
                    user_id INT NOT NULL,
                    translated_audio_id INT NOT NULL,
                    PRIMARY KEY (user_id, translated_audio_id),
                    FOREIGN KEY (user_id) REFERENCES app_user(id),
                    FOREIGN KEY (translated_audio_id) REFERENCES translated_audio(id)
                    )
                    """).update();
    }

    private void createAdminUser() {
        String hashedPassword = passwordEncoder.encode("admin");

        jdbcClient
            .sql("INSERT INTO app_user (email, user_password, user_role) VALUES (?, ?, ?)")
            .param("admin@admin.com")
            .param(hashedPassword)
            .param("ADMIN")
            .update();
    }

    public List<User> findAll() {
        return jdbcClient.sql("SELECT * FROM user")
                .query(User.class)
                .list();
    }

    public int create(User user) {
        KeyHolder keyHolder = new GeneratedKeyHolder();
        String hashedPassword = passwordEncoder.encode(user.getUserPassword());

        int updated = jdbcTemplate.update(connection -> {
            PreparedStatement ps = connection.prepareStatement(
                "INSERT INTO app_user(email, user_password, user_role) VALUES (?, ?, ?)",
                new String[]{"id"}
            );
            ps.setString(1, user.getEmail());
            ps.setString(2, hashedPassword);
            ps.setString(3, "USER");
            return ps;
        }, keyHolder);

        Assert.state(updated == 1, "Failed to create user " + user.getEmail());

        Number key = keyHolder.getKey();
        
        Assert.state(key != null, "Failed to retrieve generated ID foe user " + user.getEmail());

        return key.intValue();
    }

    public User findByEmail(String email) {
        var result = jdbcClient.sql("SELECT * FROM app_user WHERE email = :email")
            .param("email", email)
            .query(User.class)
            .optional();

        return result.orElse(null);
    }
}
