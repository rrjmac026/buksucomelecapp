package com.example.appdevfinal.models;

import com.google.firebase.firestore.DocumentId;

public class Candidate {
    @DocumentId
    private String id;
    private String name;
    private String position;
    private String partyList;
    private String platform;

    public Candidate() {}

    public Candidate(String name, String position, String partyList, String platform) {
        this.name = name;
        this.position = position;
        this.partyList = partyList;
        this.platform = platform;
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getPosition() { return position; }
    public void setPosition(String position) { this.position = position; }
    public String getPartyList() { return partyList; }
    public void setPartyList(String partyList) { this.partyList = partyList; }
    public String getPlatform() { return platform; }
    public void setPlatform(String platform) { this.platform = platform; }
}
