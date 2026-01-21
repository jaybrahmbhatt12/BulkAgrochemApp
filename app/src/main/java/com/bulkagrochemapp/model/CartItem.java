package com.bulkagrochemapp.model;

import java.io.Serializable;

public class CartItem implements Serializable {

    private int productId;
    private String name;
    private String image;
    private double price;
    private String stockType; 
    private int quantity;
    private int minQuantity;
    private double taxRate; 

    public CartItem(int productId, String name, String image, double price, int minQuantity, String stockType, double taxRate) {
        this.productId = productId;
        this.name = name;
        this.image = image;
        this.price = price;
        this.minQuantity = minQuantity;
        this.stockType = stockType;
        this.quantity = minQuantity;
        this.taxRate = taxRate;
    }

    public int getProductId() { return productId; }
    public String getName() { return name; }
    public String getImage() { return image; }
    public double getPrice() { return price; }
    public String getStockType() { return stockType; }
    public int getQuantity() { return quantity; }
    public int getMinQuantity() { return minQuantity; }

    public void setName(String name) { this.name = name; }
    public void setImage(String image) { this.image = image; }
    public void setPrice(double price) { this.price = price; }
    public void setStockType(String stockType) { this.stockType = stockType; }
    public void setMinQuantity(int minQuantity) { this.minQuantity = minQuantity; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }

    public void setQuantity(int quantity) { this.quantity = quantity; }

    public void increaseQty() { quantity += minQuantity; }
    public void decreaseQty() {
        if (quantity > minQuantity) {
            quantity -= minQuantity;
        } else {
            quantity = 0;
        }
    }

    // Total = Price * Quantity (as per website's "TOTAL" column)
    public double getTotalAmount() { return price * quantity; }
    
    // Tax = Total * TaxRate / 100
    public double getTaxAmount() { return getTotalAmount() * (taxRate / 100.0); }
    
    // Subtotal = Total + Tax (as per website's "SUBTOTAL" column)
    public double getSubtotalAmount() { return getTotalAmount() + getTaxAmount(); }
    
    public double getTaxRate() { return taxRate; }
    
    // Legacy support if needed
    public double getTotalPrice() { return getSubtotalAmount(); }
}
