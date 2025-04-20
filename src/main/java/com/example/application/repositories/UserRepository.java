package com.example.application.repositories;

import java.io.Console;
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
        createUserTable();
        createTranslatedTranscriptTable();
        createUserProfileTable();
        createAdminUser();
    }

    private void createTranslatedTranscriptTable() {
        jdbcClient.sql("""
                    CREATE TABLE IF NOT EXISTS translated_transcript (
                    id INT NOT NULL AUTO_INCREMENT,
                    user_id INT NOT NULL,
                    text_language VARCHAR(2),
                    uuid VARCHAR(36),
                    text_name VARCHAR(250) NOT NULL,
                    created_at DATETIME NOT NULL,
                    FOREIGN KEY (user_id) REFERENCES app_user(id),
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

    private void createUserProfileTable() {
        jdbcClient.sql("""
                CREATE TABLE IF NOT EXISTS user_profile (
                    user_id INT NOT NULL,
                    preferred_target_language VARCHAR(2) NOT NULL,
                    preferred_origin_language VARCHAR(2),
                    created_at DATETIME NOT NULL,
                    PRIMARY KEY (user_id),
                    FOREIGN KEY (user_id) REFERENCES app_user(id) ON DELETE CASCADE
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

    public void savePreferredLanguage(int userId, String preferredLanguage) {
        int updated = jdbcClient.sql("""
                UPDATE user_profile
                SET preferred_target_language = ?
                WHERE user_id = ?
            """)
        .params(preferredLanguage, userId)
        .update();

        if (updated == 0) {
            jdbcClient.sql("""
                INSERT INTO user_profile (user_id, preferred_target_language, created_at)
                VALUES (?, ?, CURRENT_TIMESTAMP)
                """)
                .params(userId, preferredLanguage)
                .update();
        }

        System.out.println("User settings saved successfully!");
    }
}
