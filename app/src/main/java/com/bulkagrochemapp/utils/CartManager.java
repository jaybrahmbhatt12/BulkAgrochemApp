package com.bulkagrochemapp.utils;

import com.bulkagrochemapp.model.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static final List<CartItem> cartItems = new ArrayList<>();

    // 🔹 STEP 13.3.1 — Listener interface
    public interface CartTotalListener {
        void onTotalChanged(double total);
    }

    // 🔹 STEP 13.3.2 — Listener variable
    private static CartTotalListener totalListener;

    // 🔹 STEP 13.3.3 — Setter
    public static void setCartTotalListener(CartTotalListener listener) {
        totalListener = listener;
    }

    // 🔹 STEP 13.3.4 — Notifier
    private static void notifyTotalChanged() {
        if (totalListener != null) {
            totalListener.onTotalChanged(getTotalAmount());
        }
    }

    // 🔹 Add product to cart
    public static void addToCart(CartItem newItem) {

        for (CartItem item : cartItems) {
            if (item.getProductId() == newItem.getProductId()) {
                item.increaseQty();
                notifyTotalChanged(); // ✅ added
                return;
            }
        }

        cartItems.add(newItem);
        notifyTotalChanged(); // ✅ added
    }

    // 🔹 Get cart items
    public static List<CartItem> getCartItems() {
        return cartItems;
    }

    // 🔹 Get total cart amount
    public static double getTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    // 🔹 Clear cart
    public static void clearCart() {
        cartItems.clear();
        notifyTotalChanged(); // ✅ added
    }
}
