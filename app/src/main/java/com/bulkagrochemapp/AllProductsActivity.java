package com.bulkagrochemapp;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.adapter.AllProductsCategoryAdapter;
import com.bulkagrochemapp.model.Category;
import com.bulkagrochemapp.model.CategoryResponse;
import com.bulkagrochemapp.model.Product;
import com.bulkagrochemapp.model.ProductResponse;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AllProductsActivity extends BaseActivity {

    private RecyclerView rvAllProducts;
    private ProgressBar progressBar;
    private ApiService apiService;
    private List<Category> categories = new ArrayList<>();
    private List<Product> allProducts = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_all_products);

        // Make system bar white and text dark
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Window window = getWindow();
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.setStatusBarColor(Color.WHITE);
            window.setNavigationBarColor(Color.WHITE);
            
            int flags = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                flags |= View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;
            }
            window.getDecorView().setSystemUiVisibility(flags);
        }

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }

        rvAllProducts = findViewById(R.id.rvAllProducts);
        progressBar = findViewById(R.id.progressBar);
        rvAllProducts.setLayoutManager(new LinearLayoutManager(this));

        apiService = RetrofitClient.getClient().create(ApiService.class);

        loadData();
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);
        apiService.getCategories().enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(@NonNull Call<CategoryResponse> call, @NonNull Response<CategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    categories = response.body().getData();
                    loadProducts();
                } else {
                    progressBar.setVisibility(View.GONE);
                    Toast.makeText(AllProductsActivity.this, "Failed to load categories", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<CategoryResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AllProductsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProducts() {
        apiService.getProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(@NonNull Call<ProductResponse> call, @NonNull Response<ProductResponse> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    allProducts = response.body().getData();
                    setupAdapter();
                } else {
                    Toast.makeText(AllProductsActivity.this, "Failed to load products", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(@NonNull Call<ProductResponse> call, @NonNull Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(AllProductsActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setupAdapter() {
        Map<Integer, List<Product>> categoryProductMap = new HashMap<>();
        for (Product product : allProducts) {
            int catId = product.getCategoryId();
            if (!categoryProductMap.containsKey(catId)) {
                categoryProductMap.put(catId, new ArrayList<>());
            }
            categoryProductMap.get(catId).add(product);
        }

        // Filter categories that actually have products
        List<Category> activeCategories = new ArrayList<>();
        for (Category cat : categories) {
            if (categoryProductMap.containsKey(cat.getId())) {
                activeCategories.add(cat);
            }
        }

        AllProductsCategoryAdapter adapter = new AllProductsCategoryAdapter(this, activeCategories, categoryProductMap);
        rvAllProducts.setAdapter(adapter);
    }
}