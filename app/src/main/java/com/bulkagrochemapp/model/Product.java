package com.bulkagrochemapp.model;

import java.io.Serializable;

public class Product implements Serializable {

    private int id;
    private String name;
    private String image;
    private double price;
    private double original_price;
    private int discount_percent;
    private int is_trending;
    private int is_popular;

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
        return is_trending;
    }

    public int getIsPopular() {
        return is_popular;
    }
}
