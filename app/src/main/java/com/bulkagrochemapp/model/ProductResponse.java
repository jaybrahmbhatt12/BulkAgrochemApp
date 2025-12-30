package com.bulkagrochemapp.model;

import java.util.List;

public class ProductResponse {

    private boolean status;
    private String message;
    private List<Product> data;

    public boolean isStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public List<Product> getData() {
        return data;
    }
}
