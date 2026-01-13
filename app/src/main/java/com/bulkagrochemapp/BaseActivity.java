package com.bulkagrochemapp;

import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    protected void setupBottomNavigation() {
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        if (bottomNavigationView != null) {
            // Set correct selected item based on current activity
            if (this instanceof MainActivity) {
                bottomNavigationView.getMenu().findItem(R.id.nav_home).setChecked(true);
            } else if (this instanceof CartActivity) {
                bottomNavigationView.getMenu().findItem(R.id.nav_cart).setChecked(true);
            } else if (this instanceof ProfileActivity) {
                bottomNavigationView.getMenu().findItem(R.id.nav_account).setChecked(true);
            } else if (this instanceof ProductListActivity) {
                if (getIntent().getBooleanExtra("is_sale", false)) {
                    bottomNavigationView.getMenu().findItem(R.id.nav_sale).setChecked(true);
                }
            }

            bottomNavigationView.setOnItemSelectedListener(item -> {
                int id = item.getItemId();
                Intent intent = null;

                if (id == R.id.nav_home) {
                    if (!(this instanceof MainActivity)) {
                        intent = new Intent(this, MainActivity.class);
                    }
                } else if (id == R.id.nav_cart) {
                    if (!(this instanceof CartActivity)) {
                        intent = new Intent(this, CartActivity.class);
                    }
                } else if (id == R.id.nav_sale) {
                    if (!(this instanceof ProductListActivity && getIntent().getBooleanExtra("is_sale", false))) {
                        intent = new Intent(this, ProductListActivity.class);
                        intent.putExtra("category_name", "Hot Sale");
                        intent.putExtra("is_sale", true);
                    }
                } else if (id == R.id.nav_account) {
                    if (!(this instanceof ProfileActivity)) {
                        intent = new Intent(this, ProfileActivity.class);
                    }
                }

                if (intent != null) {
                    // FLAG_ACTIVITY_REORDER_TO_FRONT brings the existing instance to top
                    // FLAG_ACTIVITY_SINGLE_TOP prevents creating a new instance if it's already on top
                    intent.setFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                    startActivity(intent);
                    overridePendingTransition(0, 0);
                    return true;
                }
                return true;
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