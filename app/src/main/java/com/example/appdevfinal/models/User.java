package com.example.appdevfinal.models;

public class User {
    private String id;
    private String email;
    private String role;
    private String studentId;
    private boolean hasVoted;
    private String name;

    public User() {} // Required for Firebase

    public User(String email, String role, String studentId) {
        this.email = email;
        this.role = role;
        this.studentId = studentId;
        this.hasVoted = false;
    }

    // Getters
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getStudentId() { return studentId; }
    public boolean hasVoted() { return hasVoted; }
    public String getName() { return name; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setHasVoted(boolean hasVoted) { this.hasVoted = hasVoted; }
    public void setName(String name) { this.name = name; }
}
