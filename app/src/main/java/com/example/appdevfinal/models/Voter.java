package com.example.appdevfinal.models;

public class Voter {
    private String id;
    private String name;
    private String studentId;
    private String course;
    private boolean hasVoted;

    public Voter() {} // Required for Firestore

    public Voter(String name, String studentId, String course) {
        this.name = name;
        this.studentId = studentId;
        this.course = course;
        this.hasVoted = false;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getCourse() { return course; }
    public void setCourse(String course) { this.course = course; }
    public boolean getHasVoted() { return hasVoted; }
    public void setHasVoted(boolean hasVoted) { this.hasVoted = hasVoted; }
}
