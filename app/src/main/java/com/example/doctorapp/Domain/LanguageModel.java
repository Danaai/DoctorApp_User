package com.example.doctorapp.Domain;

import com.google.firebase.database.PropertyName;

public class LanguageModel {

    private String name;
    @PropertyName("imgUrl")
    private String img;
    private String id;

    public LanguageModel() {

    }

    public LanguageModel(String id, String name, String img) {
        this.id = id;
        this.name = name;
        this.img = img;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @PropertyName("imgUrl")
    public String getImg() {
        return img;
    }

    @PropertyName("imgUrl")
    public void setImg(String img) {
        this.img = img;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }
}
