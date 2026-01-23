package com.bulkagrochemapp.model;

import java.util.List;

public class OrderResponse {
    private String status;
    private boolean success;
    private String message;
    private List<Order> data;

    public String getStatus() { return status; }
    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<Order> getData() { return data; }
}
