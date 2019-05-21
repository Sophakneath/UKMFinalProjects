package com.example.phakneath.jobber.Model;

public class saveESCCI {

    String id;
    String ownerID;

    public saveESCCI(){}

    public saveESCCI(String id, String ownerID) {
        this.id = id;
        this.ownerID = ownerID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }
}
