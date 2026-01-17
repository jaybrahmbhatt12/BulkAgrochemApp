package com.bulkagrochemapp.model;

import com.google.gson.annotations.SerializedName;

public class District {
    @SerializedName("id")
    private int id;
    
    @SerializedName("name")
    private String name;

    public int getId() { return id; }
    public String getName() { return name; }

    @Override
    public String toString() {
        return name;
    }
}
