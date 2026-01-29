package com.bulkagrochemapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    protected void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            // Remove listener before setting checked item to prevent recursive calls or misfiring
            bottomNavigationView.setOnItemSelectedListener(null);

            // Visibility Logic: Show only on primary tabs
            if (this instanceof MainActivity || this instanceof CartActivity || this instanceof ProfileActivity || this instanceof AllProductsActivity) {
                bottomNavigationView.setVisibility(View.VISIBLE);
            } else {
                bottomNavigationView.setVisibility(View.GONE);
            }

            // Set correct selected item based on current activity
            if (this instanceof MainActivity) {
                bottomNavigationView.setSelectedItemId(R.id.nav_home);
            } else if (this instanceof CartActivity) {
                bottomNavigationView.setSelectedItemId(R.id.nav_cart);
            } else if (this instanceof ProfileActivity) {
                bottomNavigationView.setSelectedItemId(R.id.nav_account);
            } else if (this instanceof AllProductsActivity) {
                bottomNavigationView.setSelectedItemId(R.id.nav_products);
            }

            bottomNavigationView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();

                // If the user clicks the already selected item, do nothing
                if (id == bottomNavigationView.getSelectedItemId()) {
                    return true;
                }

                Intent intent = null;

                if (id == R.id.nav_home) {
                    intent = new Intent(this, MainActivity.class);
                } else if (id == R.id.nav_cart) {
                    intent = new Intent(this, CartActivity.class);
                } else if (id == R.id.nav_products) {
                    intent = new Intent(this, AllProductsActivity.class);
                } else if (id == R.id.nav_account) {
                    intent = new Intent(this, ProfileActivity.class);
                }

                if (intent != null) {
                    // reorder to front handles the backstack history by moving existing instance to front
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                return false;
            });
        }
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        setupBottomNavigation();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupBottomNavigation();
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        setupBottomNavigation();
    }
}