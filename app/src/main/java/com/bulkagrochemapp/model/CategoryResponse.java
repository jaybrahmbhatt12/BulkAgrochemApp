package com.bulkagrochemapp.model;

import java.util.List;

public class CategoryResponse {

    private boolean status;
    private List<Category> data;

    public boolean isStatus() {
        return status;
    }

    public List<Category> getData() {
        return data;
    }
}
