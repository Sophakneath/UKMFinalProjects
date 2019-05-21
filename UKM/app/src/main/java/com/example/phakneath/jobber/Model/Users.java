package com.example.phakneath.jobber.Model;

import java.io.Serializable;
import java.util.List;

public class Users implements Serializable {

    String id;
    String email;
    String username;
    String nationality;
    String workplace;
    String position;
    String image;
    //List<MyESCCI> randomThings;
    //List<saveESCCI> favourite;

    public Users()
    {

    }

    public Users(String id, String email, String username) {
        this.id = id;
        this.email = email;
        this.username = username;
    }

    public Users(String id, String email, String username, String nationality, String workplace, String position, String image) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.nationality = nationality;
        this.workplace = workplace;
        this.position = position;
        this.image = image;
    }

    public Users(String id, String email, String username, String nationality, String workplace, String position, List<MyESCCI> randomThings, List<saveESCCI> favourite) {
        this.id = id;
        this.email = email;
        this.username = username;
        this.nationality = nationality;
        this.workplace = workplace;
        this.position = position;
        //this.randomThings = randomThings;
        //this.favourite = favourite;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getNationality() {
        return nationality;
    }

    public void setNationality(String nationality) {
        this.nationality = nationality;
    }

    public String getWorkplace() {
        return workplace;
    }

    public void setWorkplace(String workplace) {
        this.workplace = workplace;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

   /* public List<MyESCCI> getRandomThings() {
        return randomThings;
    }

    public void setRandomThings(List<MyESCCI> randomThings) {
        this.randomThings = randomThings;
    }

    public List<saveESCCI> getFavourite() {
        return favourite;
    }

    public void setFavourite(List<saveESCCI> favourite) {
        this.favourite = favourite;
    }*/

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }

}
