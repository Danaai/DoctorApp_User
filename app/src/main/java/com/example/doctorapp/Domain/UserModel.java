package com.example.doctorapp.Domain;

import com.google.firebase.database.PropertyName;

import java.io.Serializable;

public class UserModel implements Serializable {
    private int id;
    private String name, email, password, username, phone, date, gender, address, language, role;
    private Boolean isActive;

    public UserModel() {}

    public UserModel(int id, String name, String email, String username, String password, String gender, String language, String role, Boolean isActive) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.username = username;
        this.password = password;
        this.gender = gender;
        this.language = language;
        this.role = role;
        this.isActive = isActive;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public String getGender() {
        return gender;
    }

    public void setGender(String gender) {
        this.gender = gender;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getLanguage() {
        return language;
    }

    public void setLanguage(String language) {
        this.language = language;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    @PropertyName("isActive")
    public Boolean getActive() {
        return isActive;
    }

    @PropertyName("isActive")
    public void setActive(Boolean isActive) {
        this.isActive = isActive;
    }
}