package com.bulkagrochemapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.bulkagrochemapp.model.Product;
import com.bulkagrochemapp.model.ProductResponse;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class WishlistManager {
    private static final String TAG = "WishlistManager";
    private static final List<Product> wishlistItems = new ArrayList<>();

    public static void addToWishlist(Product product) {
        if (!isInWishlist(product.getId())) {
            wishlistItems.add(product);
        }
    }

    public static void addToWishlistWithSync(Context context, Product product) {
        if (!isInWishlist(product.getId())) {
            wishlistItems.add(product);
            syncAddToRemote(context, product.getId());
        }
    }

    public static void removeFromWishlist(int productId) {
        for (int i = 0; i < wishlistItems.size(); i++) {
            if (wishlistItems.get(i).getId() == productId) {
                wishlistItems.remove(i);
                break;
            }
        }
    }

    public static void removeFromWishlistWithSync(Context context, int productId) {
        removeFromWishlist(productId);
        syncRemoveFromRemote(context, productId);
    }

    public static boolean isInWishlist(int productId) {
        for (Product product : wishlistItems) {
            if (product.getId() == productId) {
                return true;
            }
        }
        return false;
    }

    public static List<Product> getWishlistItems() {
        return new ArrayList<>(wishlistItems);
    }

    public static void syncAddToRemote(Context context, int productId) {
        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String userId = prefs.getString("id", "");

        if (userId.isEmpty()) return;

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.addToWishlist(userId, productId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Added to remote wishlist");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Failed to add to remote wishlist", t);
            }
        });
    }

    public static void syncRemoveFromRemote(Context context, int productId) {
        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String userId = prefs.getString("id", "");

        if (userId.isEmpty()) return;

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.removeFromWishlist(userId, productId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Removed from remote wishlist");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Failed to remove from remote wishlist", t);
            }
        });
    }

    public static void fetchRemoteWishlist(Context context, Runnable onComplete) {
        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String userId = prefs.getString("id", "");

        if (userId.isEmpty()) {
            if (onComplete != null) onComplete.run();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getWishlist(userId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        String status = jsonObject.optString("status");
                        if ("success".equals(status)) {
                            JSONArray data = jsonObject.optJSONArray("data");
                            wishlistItems.clear(); // Clear local list before sync
                            if (data != null && data.length() > 0) {
                                List<Integer> productIds = new ArrayList<>();
                                for (int i = 0; i < data.length(); i++) {
                                    productIds.add(data.getJSONObject(i).getInt("product_id"));
                                }
                                fetchProductDetailsForWishlist(productIds, onComplete);
                            } else {
                                if (onComplete != null) onComplete.run();
                            }
                        } else {
                            if (onComplete != null) onComplete.run();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing wishlist", e);
                        if (onComplete != null) onComplete.run();
                    }
                } else {
                    if (onComplete != null) onComplete.run();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Failed to fetch remote wishlist", t);
                if (onComplete != null) onComplete.run();
            }
        });
    }

    private static void fetchProductDetailsForWishlist(List<Integer> productIds, Runnable onComplete) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Product> allProducts = response.body().getData();
                    if (allProducts != null) {
                        for (Product p : allProducts) {
                            if (productIds.contains(p.getId())) {
                                wishlistItems.add(p);
                            }
                        }
                    }
                }
                if (onComplete != null) onComplete.run();
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                if (onComplete != null) onComplete.run();
            }
        });
    }
}
