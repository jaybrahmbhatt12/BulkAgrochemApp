package com.bulkagrochemapp.model;

import java.io.Serializable;

public class CartItem implements Serializable {

    private int productId;
    private String name;
    private String image;
    private double price;
    private String stockType; // Added for correct price unit display
    private int quantity;
    private int minQuantity;

    // Constructor
    public CartItem(int productId, String name, String image, double price, int minQuantity, String stockType) {
        this.productId = productId;
        this.name = name;
        this.image = image;
        this.price = price;
        this.minQuantity = minQuantity;
        this.stockType = stockType;
        this.quantity = minQuantity;
    }

    public int getProductId() { return productId; }
    public String getName() { return name; }
    public String getImage() { return image; }
    public double getPrice() { return price; }
    public String getStockType() { return stockType; }
    public int getQuantity() { return quantity; }
    public int getMinQuantity() { return minQuantity; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    public void increaseQty() { quantity += minQuantity; }
    public void decreaseQty() {
        if (quantity > minQuantity) {
            quantity -= minQuantity;
        } else {
            quantity = 0;
        }
    }

    public double getTotalPrice() { return price * quantity; }
}
