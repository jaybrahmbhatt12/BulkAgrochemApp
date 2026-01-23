package com.bulkagrochemapp.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;
import java.util.List;

public class Order implements Serializable {
    private String id;
    private String date;
    private String status;
    private double total;
    
    @SerializedName("order_items")
    private List<CartItem> items;

    public String getId() { return id; }
    public String getDate() { return date; }
    public String getStatus() { return status; }
    public double getTotal() { return total; }
    public List<CartItem> getItems() { return items; }

    public void setStatus(String status) { this.status = status; }
}
