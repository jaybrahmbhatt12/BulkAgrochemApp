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
        notifyTotalChanged();
    }
}
