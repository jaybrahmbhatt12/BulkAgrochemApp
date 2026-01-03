package com.bulkagrochemapp.utils;

import com.bulkagrochemapp.model.CartItem;
import java.util.ArrayList;
import java.util.List;

public class CartManager {

    private static final List<CartItem> cartItems = new ArrayList<>();

    public interface CartTotalListener {
        void onTotalChanged(double total);
    }

    private static CartTotalListener totalListener;

    public static void setCartTotalListener(CartTotalListener listener) {
        totalListener = listener;
    }

    private static void notifyTotalChanged() {
        if (totalListener != null) {
            totalListener.onTotalChanged(getTotalAmount());
        }
    }

    public static void addToCart(CartItem newItem) {

        for (CartItem item : cartItems) {
            if (item.getProductId() == newItem.getProductId()) {
                item.increaseQty();
                notifyTotalChanged();
                return;
            }
        }

        cartItems.add(newItem);
        notifyTotalChanged();
    }

    // Get cart items
    public static List<CartItem> getCartItems() {
        return cartItems;
    }

    // Get total cart amount
    public static double getTotalAmount() {
        double total = 0;
        for (CartItem item : cartItems) {
            total += item.getTotalPrice();
        }
        return total;
    }

    // Clear cart
    public static void clearCart() {
        cartItems.clear();
        notifyTotalChanged(); // ✅ added
    }
}
