package com.bulkagrochemapp.model;

public class Product {

    private int id;
    private String name;
    private String image;
    private double price;
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

    public int getIsTrending() {
        return is_trending;
    }

    public int getIsPopular() {
        return is_popular;
    }
}
