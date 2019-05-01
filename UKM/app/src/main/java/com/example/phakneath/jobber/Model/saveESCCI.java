package com.example.phakneath.jobber.Model;

public class saveESCCI {

    String id;
    String ownerID;
    long saveTime;

    public saveESCCI(){}

    public saveESCCI(String id, String ownerID, long saveTime) {
        this.id = id;
        this.ownerID = ownerID;
        this.saveTime = saveTime;
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

    public long getSaveTime() {
        return saveTime;
    }

    public void setSaveTime(long saveTime) {
        this.saveTime = saveTime;
    }
}
