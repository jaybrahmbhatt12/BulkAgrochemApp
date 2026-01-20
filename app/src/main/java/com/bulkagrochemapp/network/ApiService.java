package com.bulkagrochemapp.network;

import com.bulkagrochemapp.model.CategoryResponse;
import com.bulkagrochemapp.model.LocationResponse;
import com.bulkagrochemapp.model.ProductResponse;
import com.bulkagrochemapp.model.SliderResponse;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    // All Products Images
    String IMAGE_URL = "https://bulkapi.tiffinworld.com/api/";
    String BANNER_IMAGE_URL = "https://bulkapi.tiffinworld.com/assets/";

    // Sliders
    @GET("sliders.php")
    Call<SliderResponse> getSliders();

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

    // Fetch India States and Cities from backend assets
    @GET("../assets/india_states_cities.json")
    Call<LocationResponse> getIndiaLocations();

    @FormUrlEncoded
    @POST("register.php")
    Call<ResponseBody> register(
            @Field("name") String name,
            @Field("email") String email,
            @Field("phone") String phone
    );

    @FormUrlEncoded
    @POST("login.php")
    Call<ResponseBody> login(
            @Field("phone") String phone
    );

    @FormUrlEncoded
    @POST("send_otp.php")
    Call<ResponseBody> sendOtp(
            @Field("phone") String phone
    );

    @FormUrlEncoded
    @POST("update_profile.php")
    Call<ResponseBody> updateProfile(
            @Field("id") String id,
            @Field("name") String name,
            @Field("email") String email,
            @Field("phone") String phone,
            @Field("country") String country,
            @Field("state") String state,
            @Field("city") String city,
            @Field("zip") String zip,
            @Field("address") String address
    );
}
