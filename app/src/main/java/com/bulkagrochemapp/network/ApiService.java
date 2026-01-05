package com.bulkagrochemapp.network;

import com.bulkagrochemapp.model.CategoryResponse;
import com.bulkagrochemapp.model.ProductResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface ApiService {

    // All Products Images
    String IMAGE_URL = "http://bulk-api.mahasagar.in/";

    // All Products
    @GET("products.php")
    Call<ProductResponse> getProducts();

    // Trending Products
    @GET("products.php")
    Call<ProductResponse> getTrendingProducts();

    // Categories
    @GET("categories.php")
    Call<CategoryResponse> getCategories();

    // Products by Category
    @GET("products.php")
    Call<ProductResponse> getProductsByCategory(
            @Query("category_id") int categoryId
    );

    // Product Details
    @GET("product_details.php")
    Call<ProductResponse> getProductDetails(
            @Query("product_id") int productId
    );

    // Search Products
    @GET("products.php")
    Call<ProductResponse> searchProducts(
            @Query("search") String query
    );

    // Add to Cart
    @GET("add_to_cart.php")
    Call<ProductResponse> addToCart(
            @Query("product_id") int productId,
            @Query("quantity") int quantity
    );



}

