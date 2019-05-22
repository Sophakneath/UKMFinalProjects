package com.example.phakneath.jobber.Model;

import java.io.Serializable;

public class ESCCI implements Serializable{

    String id;
    String ownerID;
    String name;
    String mode;
    String organizer;
    String eTimeStart;
    String eTimeEnd;
    String random; //can be position_30dp(for career), age(for competition_70dp), date(for events), degree(for scholarship);
    String startDate; //for internship_70dp;
    String endDate; //for internship_70dp
    String date; //when for event_70dp, deadline application for scholarship, career, internship_70dp
    String eLocation;
    String eAddress;
    boolean eAdmission;
    String eFee;
    String link;
    String about;
    long postingTime;
    String image;
    double longitute;
    double latitute;

    public ESCCI()
    {

    }

    public ESCCI(String id, String ownerID, String name, String mode, String organizer, String eTimeStart, String eTimeEnd, String random, String startDate, String endDate, String date, String eLocation, String eAddress, boolean eAdmission, String eFee, String link, String about, long postingTime) {
        this.id = id;
        this.ownerID = ownerID;
        this.name = name;
        this.mode = mode;
        this.organizer = organizer;
        this.eTimeStart = eTimeStart;
        this.eTimeEnd = eTimeEnd;
        this.random = random;
        this.startDate = startDate;
        this.endDate = endDate;
        this.date = date;
        this.eLocation = eLocation;
        this.eAddress = eAddress;
        this.eAdmission = eAdmission;
        this.eFee = eFee;
        this.link = link;
        this.about = about;
        this.postingTime = postingTime;
    }

    public ESCCI(String id, String ownerID, String name, String mode, String organizer, String date, String eTimeStart, String eTimeEnd, String eLocation, String eAddress, String link, Boolean admission, String fee, String about, long postingTime, String image, double longitute, double latitute) {
        this.id = id;
        this.ownerID = ownerID;
        this.name = name;
        this.mode = mode;
        this.organizer = organizer;
        this.date = date;
        this.eTimeStart = eTimeStart;
        this.eTimeEnd = eTimeEnd;
        this.eLocation = eLocation;
        this.eAddress = eAddress;
        this.link = link;
        this.eAdmission = admission;
        this.eFee = fee;
        this.about = about;
        this.postingTime = postingTime;
        this.image = image;
        this.longitute = longitute;
        this.latitute = latitute;
    }

    public ESCCI(String id, String ownerID, String name, String mode, String organizer, String random, String date, String link, String about, long postingTime, String image) {
        this.id = id;
        this.ownerID = ownerID;
        this.name = name;
        this.mode = mode;
        this.organizer = organizer;
        this.random = random;
        this.date = date;
        this.link = link;
        this.about = about;
        this.postingTime = postingTime;
        this.image = image;
    }

    public ESCCI(String id, String ownerID, String name, String mode, String organizer, String from, String to, String date, String link, String about, long postingTime, String image) {
        this.id = id;
        this.ownerID = ownerID;
        this.name = name;
        this.mode = mode;
        this.organizer = organizer;
        this.startDate = from;
        this.endDate = to;
        this.date = date;
        this.link = link;
        this.about = about;
        this.postingTime = postingTime;
        this.image = image;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMode() {
        return mode;
    }

    public void setMode(String mode) {
        this.mode = mode;
    }

    public String getOrganizer() {
        return organizer;
    }

    public void setOrganizer(String organizer) {
        this.organizer = organizer;
    }

    public String geteTimeStart() {
        return eTimeStart;
    }

    public void seteTimeStart(String eTimeStart) {
        this.eTimeStart = eTimeStart;
    }

    public String geteTimeEnd() {
        return eTimeEnd;
    }

    public void seteTimeEnd(String eTimeEnd) {
        this.eTimeEnd = eTimeEnd;
    }

    public String getRandom() {
        return random;
    }

    public void setRandom(String random) {
        this.random = random;
    }

    public String getStartDate() {
        return startDate;
    }

    public void setStartDate(String startDate) {
        this.startDate = startDate;
    }

    public String getEndDate() {
        return endDate;
    }

    public void setEndDate(String endDate) {
        this.endDate = endDate;
    }

    public String getApplicationDeadline() {
        return date;
    }

    public void setApplicationDeadline(String applicationDeadline) {
        this.date = applicationDeadline;
    }

    public String geteLocation() {
        return eLocation;
    }

    public void seteLocation(String eLocation) {
        this.eLocation = eLocation;
    }

    public String geteAddress() {
        return eAddress;
    }

    public void seteAddress(String eAddress) {
        this.eAddress = eAddress;
    }

    public boolean iseAdmission() {
        return eAdmission;
    }

    public void seteAdmission(boolean eAdmission) {
        this.eAdmission = eAdmission;
    }

    public String geteFee() {
        return eFee;
    }

    public void seteFee(String eFee) {
        this.eFee = eFee;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getAbout() {
        return about;
    }

    public void setAbout(String about) {
        this.about = about;
    }

    public long getPostingTime() {
        return postingTime;
    }

    public void setPostingTime(long postingTime) {
        this.postingTime = postingTime;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public double getLongitute() {
        return longitute;
    }

    public void setLongitute(double longitute) {
        this.longitute = longitute;
    }

    public double getLatitute() {
        return latitute;
    }

    public void setLatitute(double latitute) {
        this.latitute = latitute;
    }

}
