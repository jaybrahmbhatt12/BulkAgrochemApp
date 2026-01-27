package com.bulkagrochemapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CampaignResponse {
    @SerializedName("status")
    private String status;
    
    @SerializedName("campaigns")
    private List<Campaign> campaigns;

    public String getStatus() { return status; }
    public List<Campaign> getCampaigns() { return campaigns; }
}
