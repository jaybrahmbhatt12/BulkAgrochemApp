package com.bulkagrochemapp.model;

import com.google.gson.annotations.SerializedName;

public class Coupon {
    @SerializedName("id")
    private int id;
    
    @SerializedName("code")
    private String code;
    
    @SerializedName("type")
    private int type; // 1 for Percentage, 2 for Amount (assuming based on price being double)
    
    @SerializedName("price")
    private double price; // This is the discount value
    
    @SerializedName("times")
    private String times; // Quantity/Limit
    
    @SerializedName("used")
    private int used;
    
    @SerializedName("status")
    private int status;
    
    @SerializedName("start_date")
    private String startDate;
    
    @SerializedName("end_date")
    private String endDate;
    
    @SerializedName("coupon_type")
    private String couponType;
    
    @SerializedName("above_price")
    private double abovePrice;

    public int getId() { return id; }
    public String getCode() { return code; }
    public int getType() { return type; }
    public double getPrice() { return price; }
    public String getTimes() { return times; }
    public int getUsed() { return used; }
    public int getStatus() { return status; }
    public String getStartDate() { return startDate; }
    public String getEndDate() { return endDate; }
    public String getCouponType() { return couponType; }
    public double getAbovePrice() { return abovePrice; }
}
