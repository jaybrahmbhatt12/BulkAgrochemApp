package com.bulkagrochemapp.network;

import com.bulkagrochemapp.model.CategoryResponse;
import com.bulkagrochemapp.model.CouponResponse;
import com.bulkagrochemapp.model.LocationResponse;
import com.bulkagrochemapp.model.OrderResponse;
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

    // Coupons
    @GET("coupons.php")
    Call<CouponResponse> getCoupons();

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
    @FormUrlEncoded
    @POST("add_to_cart.php")
    Call<ResponseBody> addToCart(
            @Field("uid") String uid,
            @Field("pid") int pid,
            @Field("qty") int qty,
            @Field("size_price") String sizePrice,
            @Field("values") String values,
            @Field("prices") String prices,
            @Field("tax") String tax,
            @Field("total_prices") double totalPrices
    );

    // Fetch Cart
    @FormUrlEncoded
    @POST("fetch_cart.php")
    Call<ResponseBody> fetchCart(
            @Field("uid") String uid
    );

    // Delete from Cart
    @FormUrlEncoded
    @POST("delete_from_cart.php")
    Call<ResponseBody> deleteFromCart(
            @Field("uid") String uid,
            @Field("pid") int pid
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

    // Fetch dynamic pages (About Us, Privacy Policy, etc.)
    @GET("pages.php")
    Call<ResponseBody> getPageContent(
            @Query("slug") String slug
    );

    @FormUrlEncoded
    @POST("add_to_wishlist.php")
    Call<ResponseBody> addToWishlist(
            @Field("user_id") String userId,
            @Field("product_id") int productId
    );

    @FormUrlEncoded
    @POST("get_wishlist.php")
    Call<ResponseBody> getWishlist(
            @Field("user_id") String userId
    );

    @FormUrlEncoded
    @POST("delete_wishlist_item.php")
    Call<ResponseBody> removeFromWishlist(
            @Field("user_id") String userId,
            @Field("product_id") int productId
    );

    @FormUrlEncoded
    @POST("order_history.php")
    Call<OrderResponse> getOrderHistory(
            @Field("uid") String uid
    );

    @FormUrlEncoded
    @POST("cancel_order.php")
    Call<ResponseBody> cancelOrder(
            @Field("order_id") String orderId,
            @Field("uid") String uid
    );

    @FormUrlEncoded
    @POST("return_order.php")
    Call<ResponseBody> returnOrder(
            @Field("order_id") String orderId,
            @Field("uid") String uid,
            @Field("reason") String reason
    );
}
