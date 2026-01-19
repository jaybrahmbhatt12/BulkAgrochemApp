package com.bulkagrochemapp.utils;

import com.bulkagrochemapp.model.Product;
import java.util.ArrayList;
import java.util.List;

public class WishlistManager {
    private static final List<Product> wishlistItems = new ArrayList<>();

    public static void addToWishlist(Product product) {
        if (!isInWishlist(product.getId())) {
            wishlistItems.add(product);
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
}
