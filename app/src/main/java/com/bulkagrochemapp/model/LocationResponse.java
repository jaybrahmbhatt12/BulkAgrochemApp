package com.bulkagrochemapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class LocationResponse {
    @SerializedName("states")
    private List<State> states;

    public List<State> getStates() {
        return states;
    }

    public void setStates(List<State> states) {
        this.states = states;
    }
}
