package com.example.phakneath.jobber.Model;

public class MyESCCI {

    private String id;
    private String mode;
    private String ownerID;

    public MyESCCI(){}

    public MyESCCI(String id, String mode, String ownerID) {
        this.id = id;
        this.mode = mode;
        this.ownerID = ownerID;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getOwnerID() {
        return ownerID;
    }

    public void setOwnerID(String ownerID) {
        this.ownerID = ownerID;
    }
}
