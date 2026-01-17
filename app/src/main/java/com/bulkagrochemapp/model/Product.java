package com.bulkagrochemapp.model;

import com.google.gson.annotations.SerializedName;


import java.io.Serializable;

public class Product implements Serializable {

    private int id;
    private String name;
    private String image;
    private double price;

    @SerializedName("category_id")
    private int categoryId;
    
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

    @SerializedName("minimum_qty")
    private int minimum_qty;

    @SerializedName("is_latest")
    private int isLatest;

    @SerializedName("is_big")
    private int isBig;

    @SerializedName("is_top")
    private int isTop;

    @SerializedName("whole_sell_qty")
    private String wholeSellQty;

    @SerializedName("whole_sell_discount")
    private String wholeSellDiscount;

    @SerializedName("taxpro")
    private String taxpro;


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

    public int getIsLatest() {
        return isLatest;
    }

    public int getIsBig() {
        return isBig;
    }

    public int getIsTop() {
        return isTop;
    }

    public int getMinimumQty() {
        return minimum_qty;
    }

    public String getWholeSellQty() {
        return wholeSellQty;
    }

    public String getWholeSellDiscount() {
        return wholeSellDiscount;
    }

    public int getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(int categoryId) {
        this.categoryId = categoryId;
    }

    public String getTaxpro() {
        return taxpro;
    }

    public double getTaxRate() {
        try {
            if (taxpro != null && !taxpro.isEmpty()) {
                return Double.parseDouble(taxpro);
            }
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return 18.0; // Default if not found or invalid
    }
}
