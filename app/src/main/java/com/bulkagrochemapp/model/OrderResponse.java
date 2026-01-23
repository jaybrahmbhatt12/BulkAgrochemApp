package com.bulkagrochemapp.model;

import java.util.List;

public class OrderResponse {
    private boolean success;
    private String message;
    private List<Order> data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public List<Order> getData() { return data; }
}
