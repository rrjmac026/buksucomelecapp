package com.example.appdevfinal.models;

public class User {
    private String uid;
    private String name;
    private String email;
    private String studentId;
    private String role;

    public User() {} // Required for Firebase

    public User(String uid, String name, String email, String studentId, String role) {
        this.uid = uid;
        this.name = name;
        this.email = email;
        this.studentId = studentId;
        this.role = role;
    }

    public String getUid() { return uid; }
    public void setUid(String uid) { this.uid = uid; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getStudentId() { return studentId; }
    public void setStudentId(String studentId) { this.studentId = studentId; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}
