package com.bulkagrochemapp.model;

import java.io.Serializable;

public class CartItem implements Serializable {

    private int productId;
    private String name;
    private String image;
    private double price; // This will be the current price based on quantity
    private double basePrice; // Original product price
    private String stockType; 
    private int quantity;
    private int minQuantity;
    private double taxRate; 
    private String wholeSellQty;
    private String wholeSellDiscount;

    public CartItem(int productId, String name, String image, double price, int minQuantity, String stockType, double taxRate) {
        this.productId = productId;
        this.name = name;
        this.image = image;
        this.basePrice = price;
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
    public String getWholeSellQty() { return wholeSellQty; }
    public String getWholeSellDiscount() { return wholeSellDiscount; }

    public void setName(String name) { this.name = name; }
    public void setImage(String image) { this.image = image; }
    public void setPrice(double price) { this.price = price; }
    public void setBasePrice(double basePrice) { this.basePrice = basePrice; }
    public void setStockType(String stockType) { this.stockType = stockType; }
    public void setMinQuantity(int minQuantity) { this.minQuantity = minQuantity; }
    public void setTaxRate(double taxRate) { this.taxRate = taxRate; }
    public void setWholeSellQty(String wholeSellQty) { this.wholeSellQty = wholeSellQty; }
    public void setWholeSellDiscount(String wholeSellDiscount) { this.wholeSellDiscount = wholeSellDiscount; }

    public void setQuantity(int quantity) { 
        this.quantity = quantity;
        updateTieredPrice();
    }

    public void increaseQty() { 
        quantity += minQuantity;
        updateTieredPrice();
    }

    public void decreaseQty() {
        if (quantity > minQuantity) {
            quantity -= minQuantity;
        } else {
            quantity = 0;
        }
        updateTieredPrice();
    }

    private void updateTieredPrice() {
        this.price = calculateTieredPrice(this.quantity);
    }

    private double calculateTieredPrice(int qty) {
        double currentPrice = basePrice; 
        if (wholeSellQty != null && wholeSellDiscount != null) {
            String[] qtyArray = wholeSellQty.split(",");
            String[] priceArray = wholeSellDiscount.split(",");
            int length = Math.min(qtyArray.length, priceArray.length);

            int bestQtyMatch = -1;
            for (int i = 0; i < length; i++) {
                try {
                    int tierQty = Integer.parseInt(qtyArray[i].trim());
                    if (qty >= tierQty && tierQty > bestQtyMatch) {
                        currentPrice = Double.parseDouble(priceArray[i].trim());
                        bestQtyMatch = tierQty;
                    }
                } catch (Exception ignored) {}
            }
        }
        return currentPrice;
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
