package com.example.appdevfinal.models;

public class Voter {
    private String id;
    private String email;
    private String role;
    private String studentId;
    private boolean hasVoted;

    public Voter() {} // Required for Firebase

    public Voter(String id, String email, String studentId) {
        this.id = id;
        this.email = email;
        this.studentId = studentId;
        this.role = "voter";
        this.hasVoted = false;
    }

    // Getters
    public String getId() { return id; }
    public String getEmail() { return email; }
    public String getRole() { return role; }
    public String getStudentId() { return studentId; }
    public boolean hasVoted() { return hasVoted; }

    // Setters
    public void setId(String id) { this.id = id; }
    public void setEmail(String email) { this.email = email; }
    public void setRole(String role) { this.role = role; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public void setHasVoted(boolean hasVoted) { this.hasVoted = hasVoted; }
}
