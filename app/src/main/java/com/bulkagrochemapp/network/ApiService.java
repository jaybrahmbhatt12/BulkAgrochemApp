package com.bulkagrochemapp.network;

import com.bulkagrochemapp.model.CampaignResponse;
import com.bulkagrochemapp.model.CategoryResponse;
import com.bulkagrochemapp.model.CouponResponse;
import com.bulkagrochemapp.model.LocationResponse;
import com.bulkagrochemapp.model.OrderResponse;
import com.bulkagrochemapp.model.ProductResponse;
import com.bulkagrochemapp.model.SliderResponse;

import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.Query;

public interface ApiService {

    String IMAGE_URL = "https://bulkagrochem.com/api";
    String BANNER_IMAGE_URL = "https://bulkagrochem.com/assets/";

    // Sliders
    @GET("front/slider-list")
    Call<SliderResponse> getSliders();

    // Campaigns
    @GET("front/campaign-list")
    Call<CampaignResponse> getCampaigns();

    // All Products
    @GET("front/product-list")
    Call<ProductResponse> getProducts();

    // Coupons
    @GET("front/coupon-list")
    Call<CouponResponse> getCoupons();

    // Trending Products
    @GET("front/product-list")
    Call<ProductResponse> getTrendingProducts();

    // Categories
    @GET("front/category-list")
    Call<CategoryResponse> getCategories();

    // Products by Category
    @GET("front/category-products")
    Call<ProductResponse> getProductsByCategory(
            @Query("category_id") int categoryId
    );

    // Product Details
    @GET("product_details.php")
    Call<ProductResponse> getProductDetails(
            @Query("product_id") int productId
    );

    // Search Products
    @GET("front/product-list")
    Call<ProductResponse> searchProducts(
            @Query("search") String query
    );

    // Add to Cart
    @FormUrlEncoded
    @POST("user/add-to-cart")
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
    @POST("user/fetch-cart")
    Call<ResponseBody> fetchCart(
            @Field("uid") String uid
    );

    // Delete from Cart
    @FormUrlEncoded
    @POST("user/delete-from-cart")
    Call<ResponseBody> deleteFromCart(
            @Field("uid") String uid,
            @Field("pid") int pid
    );

    // Clear Cart
    @FormUrlEncoded
    @POST("clear_cart.php")
    Call<ResponseBody> clearCart(
            @Field("uid") String uid
    );

    // Fetch India States and Cities from backend assets
    @GET("front/states")
    Call<LocationResponse> getIndiaLocations();

    @GET("front/cities")
    Call<LocationResponse> getCities();

    @FormUrlEncoded
    @POST("user/phone/register")
    Call<ResponseBody> register(
            @Field("name") String name,
            @Field("email") String email,
            @Field("phone") String phone
    );

    @FormUrlEncoded
    @POST("user/phone/login")
    Call<ResponseBody> login(
            @Field("phone") String phone
    );

    @FormUrlEncoded
    @POST("user/phone/send-otp")
    Call<ResponseBody> sendOtp(
            @Field("phone") String phone
    );

    @FormUrlEncoded
    @POST("user/update/profile")
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
    @GET("front/pages")
    Call<ResponseBody> getPageContent(
            @Query("slug") String slug
    );

    @FormUrlEncoded
    @POST("front/add-wishlist")
    Call<ResponseBody> addToWishlist(
            @Field("user_id") String userId,
            @Field("product_id") int productId
    );

    @GET("front/get-wishlist")
    Call<ResponseBody> getWishlist(
            @Query("user_id") String userId
    );

    @FormUrlEncoded
    @POST("front/delete-wishlist")
    Call<ResponseBody> removeFromWishlist(
            @Field("user_id") String userId,
            @Field("product_id") int productId
    );

    @FormUrlEncoded
    @POST("user/fetch-orders")
    Call<OrderResponse> fetchOrders(
            @Field("user_id") String userId
    );

    @Headers("Content-Type: application/json")
    @POST("user/order-place")
    Call<ResponseBody> placeOrder(@Body RequestBody body);

    @FormUrlEncoded
    @POST("user/order-place")
    Call<ResponseBody> updatePaymentStatus(
            @Field("order_id") int orderId,
            @Field("payment_status") String paymentStatus
    );

    @FormUrlEncoded
    @POST("order_history.php")
    Call<OrderResponse> getOrderHistory(
            @Field("uid") String uid
    );

    @FormUrlEncoded
    @POST("user/cancel-order")
    Call<ResponseBody> cancelOrder(
            @Field("order_id") int orderId,
            @Field("user_id") String userId
    );

    @FormUrlEncoded
    @POST("return_order.php")
    Call<ResponseBody> returnOrder(
            @Field("order_id") int orderId,
            @Field("uid") String uid,
            @Field("reason") String reason
    );
}
