package com.bulkagrochemapp.model;

public class CartItem {

    private int productId;
    private String name;
    private String image;
    private double price;
    private int quantity;

    // 🔹 Constructor
    public CartItem(int productId, String name, String image, double price) {
        this.productId = productId;
        this.name = name;
        this.image = image;
        this.price = price;
        this.quantity = 1;
    }

    // 🔹 REQUIRED GETTERS
    public int getProductId() {
        return productId;
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

    public int getQuantity() {
        return quantity;
    }

    // 🔹 Quantity helpers
    public void increaseQty() {
        quantity++;
    }

    public void decreaseQty() {
        if (quantity > 1) {
            quantity--;
        }
    }

    // 🔹 Total price
    public double getTotalPrice() {
        return price * quantity;
    }
}
