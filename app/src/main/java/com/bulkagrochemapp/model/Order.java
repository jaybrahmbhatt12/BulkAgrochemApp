package com.bulkagrochemapp.model;

import com.google.gson.annotations.SerializedName;
import java.io.Serializable;

public class Order implements Serializable {
    private int id;
    @SerializedName("user_id")
    private int userId;
    private String cart;
    private String method;
    private String shipping;
    @SerializedName("pickup_location")
    private String pickupLocation;
    private String totalQty;
    @SerializedName("pay_amount")
    private double payAmount;
    private String txnid;
    @SerializedName("order_number")
    private String orderNumber;
    @SerializedName("payment_status")
    private String paymentStatus;
    @SerializedName("customer_email")
    private String customerEmail;
    @SerializedName("customer_name")
    private String customerName;
    @SerializedName("customer_phone")
    private String customerPhone;
    @SerializedName("customer_address")
    private String customerAddress;
    @SerializedName("customer_city")
    private String customerCity;
    @SerializedName("customer_zip")
    private String customerZip;
    @SerializedName("shipping_name")
    private String shippingName;
    @SerializedName("shipping_address")
    private String shippingAddress;
    private String status;
    @SerializedName("created_at")
    private String createdAt;
    @SerializedName("shipping_cost")
    private double shippingCost;
    private double tax;
    @SerializedName("total_prices")
    private double totalPrice;

    // Getters
    public int getId() { return id; }
    public int getUserId() { return userId; }
    public String getCart() { return cart; }
    public String getMethod() { return method; }
    public String getShipping() { return shipping; }
    public String getPickupLocation() { return pickupLocation; }
    public String getTotalQty() { return totalQty; }
    public double getPayAmount() { return payAmount; }
    public String getTxnid() { return txnid; }
    public String getOrderNumber() { return orderNumber; }
    public String getPaymentStatus() { return paymentStatus; }
    public String getCustomerEmail() { return customerEmail; }
    public String getCustomerName() { return customerName; }
    public String getCustomerPhone() { return customerPhone; }
    public String getCustomerAddress() { return customerAddress; }
    public String getCustomerCity() { return customerCity; }
    public String getCustomerZip() { return customerZip; }
    public String getShippingName() { return shippingName; }
    public String getShippingAddress() { return shippingAddress; }
    public String getStatus() { return status; }
    public String getCreatedAt() { return createdAt; }
    public double getShippingCost() { return shippingCost; }
    public double getTax() { return tax; }
    public double getTotalPrice() { return totalPrice; }
}
