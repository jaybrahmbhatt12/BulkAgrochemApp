package com.bulkagrochemapp.model;

public class Product {

    private int id;
    private String name;
    private String image;
    private double price;
    private double original_price;
    private int discount_percent;

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
}
