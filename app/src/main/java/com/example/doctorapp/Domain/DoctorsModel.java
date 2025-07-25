package com.example.doctorapp.Domain;

import java.io.Serializable;

public class DoctorsModel implements Serializable {
    private String Address, Biography, Name, Picture, Date, Time, Location, Mobile, Patients, Site, Email, Cost;
    private int Id, Special, Experience;
    private double Rating;

    public DoctorsModel() {

    }

    public DoctorsModel(int Id, String Name, int Special, String Picture) {
        this.Id = Id;
        this.Name = Name;
        this.Special = Special;
        this.Picture = Picture;
    }

    public String getAddress() {
        return Address;
    }

    public void setAddress(String address) {
        Address = address;
    }

    public String getBiography() {
        return Biography;
    }

    public void setBiography(String biography) {
        Biography = biography;
    }

    public int getId() {
        return Id;
    }

    public void setId(int id) {
        Id = id;
    }

    public String getName() {
        return Name;
    }

    public void setName(String name) {
        Name = name;
    }

    public String getPicture() {
        return Picture;
    }

    public void setPicture(String picture) {
        Picture = picture;
    }

    public int getSpecial() {
        return Special;
    }

    public void setSpecial(int special) {
        Special = special;
    }

    public int getExperience() {
        return Experience;
    }

    public void setExperience(int experience) {
        Experience = experience;
    }

    public String getCost() {
        return Cost;
    }

    public void setCost(String cost) {
        Cost = cost;
    }

    public String getDate() {
        return Date;
    }

    public void setDate(String date) {
        Date = date;
    }

    public String getTime() {
        return Time;
    }

    public void setTime(String time) {
        Time = time;
    }

    public String getLocation() {
        return Location;
    }

    public void setLocation(String location) {
        Location = location;
    }

    public String getMobile() {
        return Mobile;
    }

    public void setMobile(String mobile) {
        Mobile = mobile;
    }

    public String getPatients() {
        return Patients;
    }

    public void setPatients(String patients) {
        Patients = patients;
    }

    public double getRating() {
        return Rating;
    }

    public void setRating(double rating) {
        Rating = rating;
    }
    public String getSite() {
        return Site;
    }
    public void setSite(String site) {
        Site = site;
    }
    public String getEmail() { return Email; }
    public void setEmail(String email) { Email = email; }
}
