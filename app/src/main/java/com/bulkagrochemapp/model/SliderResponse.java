package com.bulkagrochemapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class SliderResponse {
    @SerializedName("status")
    private String status;
    
    @SerializedName("sliders")
    private List<Slider> sliders;

    public String getStatus() {
        return status;
    }

    public List<Slider> getSliders() {
        return sliders;
    }
}
