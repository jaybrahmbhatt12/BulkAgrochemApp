package com.bulkagrochemapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class State {
    @SerializedName("id")
    private int id;
    
    @SerializedName("name")
    private String name;
    
    @SerializedName("districts")
    private List<District> districts;

    public int getId() { return id; }
    public String getName() { return name; }
    public List<District> getDistricts() { return districts; }

    @Override
    public String toString() {
        return name;
    }
}
