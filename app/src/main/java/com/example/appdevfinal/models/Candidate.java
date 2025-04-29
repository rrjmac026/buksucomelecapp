package com.example.appdevfinal.models;

public class Candidate {
    private String id;
    private String name;
    private String position;
    private String partyList;
    private String platform;
    
    public Candidate() {} // Required for Firebase
    
    public Candidate(String id, String name, String position, String partyList) {
        this.id = id;
        this.name = name;
        this.position = position;
        this.partyList = partyList;
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getPosition() { return position; }
    public String getPartyList() { return partyList; }
    public String getPlatform() { return platform; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setPosition(String position) { this.position = position; }
    public void setPartyList(String partyList) { this.partyList = partyList; }
    public void setPlatform(String platform) { this.platform = platform; }
}
