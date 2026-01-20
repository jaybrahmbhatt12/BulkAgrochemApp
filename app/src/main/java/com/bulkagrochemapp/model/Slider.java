package com.bulkagrochemapp.model;

import com.google.gson.annotations.SerializedName;

public class Slider {
    @SerializedName("id")
    private String id;
    
    @SerializedName("photo")
    private String photo;

    public String getId() {
        return id;
    }

    public String getPhoto() {
        return photo;
    }
}
