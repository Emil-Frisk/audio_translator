package com.example.application.repositories;

import java.io.Console;
import java.sql.PreparedStatement;
import java.util.List;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;

import com.example.application.data.User;
import com.example.application.data.UserProfile;

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
                email VARCHAR(250) NOT NULL UNIQUE,
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
        // Check if admin user already exists
        long adminCount = jdbcClient
            .sql("SELECT COUNT(*) FROM app_user WHERE email = ?")
            .param("admin@admin.com")
            .query(Long.class)
            .single();
    
        // Only create admin if it doesn't exist
        if (adminCount == 0) {
            String hashedPassword = passwordEncoder.encode("admin");
    
            jdbcClient
                .sql("INSERT INTO app_user (email, user_password, user_role) VALUES (?, ?, ?)")
                .param("admin@admin.com")
                .param(hashedPassword)
                .param("ADMIN")
                .update();
        }
    }

    public List<User> findAll() {
        return jdbcClient.sql("SELECT * FROM user")
                .query(User.class)
                .list();
    }

    public void create(User user) {

        String hashedPassword = passwordEncoder.encode(user.getUserPassword());

        var updated = jdbcClient.sql("""
                INSERT INTO app_user(email, user_password, user_role)
                VALUES (?, ?, ?)
            """)
            .params(user.getEmail(), hashedPassword, "USER")
            .update();
    }

    public User findByEmail(String email) {
        var result = jdbcClient.sql("SELECT * FROM app_user WHERE email = :email")
            .param("email", email)
            .query(User.class)
            .optional();

        return result.orElse(null);
    }

    public String getPreferredLanguage(int userId) {
        var result = jdbcClient.sql("""
                SELECT preferred_target_language FROM user_profile
                WHERE user_id = :user_id
                """)
                .param("user_id", userId)
                .query(String.class)
                .optional()
                .orElse(null);

        return result;
    }

    public boolean savePreferredLanguage(int userId, String preferredLanguage) {
        int updated = jdbcClient.sql("""
                UPDATE user_profile
                SET preferred_target_language = ?
                WHERE user_id = ?
            """)
        .params(preferredLanguage, userId)
        .update();

        if (updated == 0) {
            int updatedd = jdbcClient.sql("""
                INSERT INTO user_profile (user_id, preferred_target_language, created_at)
                VALUES (?, ?, CURRENT_TIMESTAMP)
                """)
                .params(userId, preferredLanguage)
                .update();
            
            if (updatedd != 0) {
                System.out.println("User settings saved successfully!");
                return true;
            } else {
                System.out.println("Something went wrong while saving user settings");
                return false;
            }
        } else {
            return true;
        }
    }

    public boolean deleteSettings(int userId) {
        int updated = jdbcClient.sql("""
                DELETE FROM user_profile 
                WHERE user_id = ?
                """)
                .params(userId)
                .update();

        if (updated != 0) {
            System.out.println("User settings deleted successfully!");
            return true;
        } else {
            System.out.println("Something went wrong while deleting user settings");
            return false;
        }
    }
}
