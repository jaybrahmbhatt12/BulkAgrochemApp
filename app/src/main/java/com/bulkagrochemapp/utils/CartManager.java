package com.bulkagrochemapp.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.bulkagrochemapp.model.CartItem;
import com.bulkagrochemapp.model.Product;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CartManager {

    private static final List<CartItem> cartItems = new ArrayList<>();
    private static List<Product> cachedProducts = new ArrayList<>();
    private static final String TAG = "CartManager";

    public interface CartTotalListener {
        void onTotalChanged(double total);
    }

    public interface OnCartDataChangedListener {
        void onDataChanged();
    }

    private static CartTotalListener totalListener;
    private static final List<OnCartDataChangedListener> dataChangedListeners = new ArrayList<>();

    public static void setCartTotalListener(CartTotalListener listener) {
        totalListener = listener;
    }

    public static void addDataChangedListener(OnCartDataChangedListener listener) {
        if (!dataChangedListeners.contains(listener)) {
            dataChangedListeners.add(listener);
        }
    }

    public static void removeDataChangedListener(OnCartDataChangedListener listener) {
        dataChangedListeners.remove(listener);
    }

    private static void notifyListeners() {
        for (OnCartDataChangedListener listener : new ArrayList<>(dataChangedListeners)) {
            listener.onDataChanged();
        }
        notifyTotalChanged();
    }

    private static void notifyTotalChanged() {
        if (totalListener != null) {
            totalListener.onTotalChanged(getTotalAmount());
        }
    }

    public static void addItem(CartItem newItem) {
        boolean exists = false;
        for (CartItem item : cartItems) {
            if (item.getProductId() == newItem.getProductId()) {
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                exists = true;
                break;
            }
        }

        if (!exists) {
            cartItems.add(newItem);
        }
        notifyListeners();
    }

    public static void addToCart(Context context, CartItem newItem) {
        boolean exists = false;
        for (CartItem item : cartItems) {
            if (item.getProductId() == newItem.getProductId()) {
                // Corrected: Add the specific quantity from newItem instead of just incrementing by minQuantity
                item.setQuantity(item.getQuantity() + newItem.getQuantity());
                exists = true;
                saveCartToDatabase(context, item);
                break;
            }
        }

        if (!exists) {
            cartItems.add(newItem);
            saveCartToDatabase(context, newItem);
        }
        
        notifyListeners();
    }

    public static void updateCartItem(Context context, CartItem item) {
        saveCartToDatabase(context, item);
        notifyListeners();
    }

    public static void removeFromCartSync(Context context, int productId) {
        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String uid = prefs.getString("id", null);
        if (uid == null) return;

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.deleteFromCart(uid, productId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Item deleted from database cart for PID: " + productId);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Error deleting from database: " + t.getMessage());
            }
        });
    }

    private static void saveCartToDatabase(Context context, CartItem item) {
        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String uid = prefs.getString("id", null);

        if (uid == null) return;

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.addToCart(
                uid,
                item.getProductId(),
                item.getQuantity(),
                String.valueOf(item.getPrice()),
                "", // values
                String.valueOf(item.getPrice()),
                String.valueOf(item.getTaxAmount()),
                item.getSubtotalAmount()
        ).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Log.d(TAG, "Item saved to database cart for PID: " + item.getProductId());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Error saving to database: " + t.getMessage());
            }
        });
    }

    public static void fetchCartFromDatabase(Context context, Runnable onComplete) {
        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String uid = prefs.getString("id", null);

        if (uid == null) {
            if (onComplete != null) onComplete.run();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.fetchCart(uid).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonString);
                        String status = jsonObject.optString("status");

                        if ("success".equals(status)) {
                            JSONArray data = jsonObject.optJSONArray("data");
                            if (data != null) {
                                cartItems.clear();
                                for (int i = 0; i < data.length(); i++) {
                                    JSONObject obj = data.getJSONObject(i);
                                    int pid = obj.optInt("pid");
                                    int qty = obj.optInt("qty");
                                    double price = obj.optDouble("size_price");
                                    
                                    CartItem item = new CartItem(pid, "Loading...", "", price, 1, "", 18.0);
                                    item.setQuantity(qty);
                                    cartItems.add(item);
                                }
                                // Try to sync with cache immediately if available
                                if (!cachedProducts.isEmpty()) {
                                    syncWithProducts(cachedProducts);
                                }
                            }
                        }
                    } catch (IOException | JSONException e) {
                        Log.e(TAG, "Error parsing cart JSON: " + e.getMessage());
                    }
                }
                if (onComplete != null) onComplete.run();
                notifyListeners();
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.e(TAG, "Fetch cart failed: " + t.getMessage());
                if (onComplete != null) onComplete.run();
            }
        });
    }

    public static void syncWithProducts(List<Product> allProducts) {
        if (allProducts == null || allProducts.isEmpty()) return;
        cachedProducts = allProducts;
        
        boolean changed = false;
        for (CartItem item : cartItems) {
            // Only update if it's still in "Loading..." state or missing details
            if (item.getName().equals("Loading...") || item.getImage().isEmpty()) {
                for (Product p : allProducts) {
                    if (p.getId() == item.getProductId()) {
                        item.setName(p.getName());
                        item.setImage(p.getImage());
                        item.setPrice(p.getPrice());
                        item.setStockType(p.getStocktype());
                        item.setMinQuantity(p.getMinimumQty());
                        item.setTaxRate(p.getTaxRate());
                        changed = true;
                        break;
                    }
                }
            }
        }
        if (changed) {
            notifyListeners();
        }
    }

    public static List<Product> getCachedProducts() {
        return cachedProducts;
    }

    // Get cart items
    public static List<CartItem> getCartItems() {
        return cartItems;
    }

    // Grand Total (Sum of all item subtotals including tax)
    public static double getTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getSubtotalAmount();
        }
        return total;
    }

    // Sum of all items Price * Qty (before tax)
    public static double getSubtotalBeforeTax() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalAmount();
        }
        return total;
    }

    // Sum of all tax amounts
    public static double getTotalTaxAmount() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTaxAmount();
        }
        return total;
    }

    // Clear cart
    public static void clearCart() {
        cartItems.clear();
        notifyListeners();
    }
}
