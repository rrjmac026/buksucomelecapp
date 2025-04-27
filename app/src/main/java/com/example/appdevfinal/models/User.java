package com.example.appdevfinal.models;

public class User {
    private String id;
    private String email;
    private String role;
    private String name;

    public User() {} // Required for Firebase

    public User(String email, String role) {
        this.email = email;
        this.role = role;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
}
