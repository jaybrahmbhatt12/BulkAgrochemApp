package com.bulkagrochemapp.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class CouponResponse {
    @SerializedName("status")
    private String status;
    @SerializedName("data")
    private List<Coupon> data;

    public String getStatus() { return status; }
    public List<Coupon> getData() { return data; }
}
