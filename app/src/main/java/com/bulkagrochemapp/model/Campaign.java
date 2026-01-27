package com.bulkagrochemapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class Campaign {
    @SerializedName("id")
    private String id;
    
    @SerializedName("campaign_name")
    private String campaignName;
    
    @SerializedName("theme")
    private String theme;
    
    @SerializedName("gradient_theme")
    private String gradientTheme;
    
    @SerializedName("custom_hex")
    private String customHex;
    
    @SerializedName("offer_banner")
    private String offerBanner;
    
    @SerializedName("welcome_message")
    private String welcomeMessage;
    
    @SerializedName("tagline")
    private String tagline;
    
    @SerializedName("offer_code")
    private String offerCode;
    
    @SerializedName("offer_description")
    private String offerDescription;
    
    @SerializedName("start_date")
    private String startDate;
    
    @SerializedName("end_date")
    private String endDate;
    
    @SerializedName("offer_details")
    private List<OfferDetail> offerDetails;

    public String getId() { return id; }
    public String getCampaignName() { return campaignName; }
    public String getTheme() { return theme; }
    public String getGradientTheme() { return gradientTheme; }
    public String getCustomHex() { return customHex; }
    public String getOfferBanner() { return offerBanner; }
    public String getWelcomeMessage() { return welcomeMessage; }
    public String getTagline() { return tagline; }
    public String getOfferCode() { return offerCode; }
    public String getOfferDescription() { return offerDescription; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public List<OfferDetail> getOfferDetails() { return offerDetails; }

    public static class OfferDetail {
        @SerializedName("key")
        private String key;
        
        @SerializedName("value")
        private String value;

        public String getKey() { return key; }
        public String getValue() { return value; }
    }
}
