package com.example.appdevfinal.models;

public class Voter {
    private String id;
    private String name;
    private String email;
    private String studentId;
    private boolean hasVoted;

    public Voter() {} // Required for Firebase

    public Voter(String id, String name, String email, String studentId) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.studentId = studentId;
        this.hasVoted = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public boolean hasVoted() { return hasVoted; }
    public void setHasVoted(boolean hasVoted) { this.hasVoted = hasVoted; }
}
