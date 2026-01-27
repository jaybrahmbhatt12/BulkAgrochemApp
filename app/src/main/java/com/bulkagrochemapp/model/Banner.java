package com.bulkagrochemapp.model;

import com.google.gson.annotations.SerializedName;

public class Banner {
    @SerializedName("id")
    private String id;
    
    @SerializedName("photo")
    private String photo;
    
    @SerializedName("link")
    private String link;
    
    @SerializedName("type")
    private String type;
    
    @SerializedName("title")
    private String title;
    
    @SerializedName("text")
    private String text;

    public String getId() { return id; }
    public String getPhoto() { return photo; }
    public String getLink() { return link; }
    public String getType() { return type; }
    public String getTitle() { return title; }
    public String getText() { return text; }
}
