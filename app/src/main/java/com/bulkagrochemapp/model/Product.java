package com.bulkagrochemapp.model;

import com.google.gson.annotations.SerializedName;


import java.io.Serializable;

public class Product implements Serializable {

    private int id;
    private String name;
    private String image;
    private double price;
    
    @SerializedName(value = "description", alternate = {"desc", "short_description"})
    private String description;

    @SerializedName("details")
    private String details;

    @SerializedName("original_price")
    private double original_price;

    @SerializedName("stocktype")
    private String stocktype;

    @SerializedName("is_hot")
    private int isHot;

    @SerializedName("discount_percent")
    private int discount_percent;

    @SerializedName(value = "trending", alternate = {"is_trending", "isTrending"})
    private int isTrending;

    @SerializedName(value = "popular", alternate = {"is_popular", "isPopular"})
    private int isPopular;

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getImage() {
        return image;
    }

    public double getPrice() {
        return price;
    }

    public double getOriginalPrice() {
        return original_price;
    }

    public int getDiscountPercent() {
        return discount_percent;
    }

    public int getIsTrending() {
        return isTrending;
    }

    public int getIsPopular() {
        return isPopular;
    }

    public String getDescription() {
        return description;
    }

    public String getDetails() {
        return details;
    }

    public String getStocktype() {
        return stocktype;
    }

    public int getIsHot() {
        return isHot;
    }
}
