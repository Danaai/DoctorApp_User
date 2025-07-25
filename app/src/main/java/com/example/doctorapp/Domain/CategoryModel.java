package com.example.doctorapp.Domain;

public class CategoryModel {
    private int Id;
    private String Name;
    private String Picture;

    public CategoryModel() {

    }

    public CategoryModel(int Id, String Name, String Picture) {
        this.Id = Id;
        this.Name = Name;
        this.Picture = Picture;
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

}
