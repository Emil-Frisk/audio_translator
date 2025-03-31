package com.example.application.data;

public class User {
    private int id;
    private String email;
    private String userPassword;
    private String userRole;

    public User() {
        
    }

    public User(int id, String email, String userPassword, String userRole) {
        this.id = id;
        this.email = email;
        this.userPassword = userPassword;
        this.userRole = userRole;
    }

    public String getUserPassword() {
        return userPassword;
    }

    public void setUserPassword(String userPassword) {
        this.userPassword = userPassword;
    }

    public String getUserRole() {
        return userRole;
    }

    public void setUserRole(String userRole) {
        this.userRole = userRole;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
