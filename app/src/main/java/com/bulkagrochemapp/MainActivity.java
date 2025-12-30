package com.bulkagrochemapp;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.adapter.CategoryAdapter;
import com.bulkagrochemapp.model.CategoryResponse;
import com.bulkagrochemapp.model.ProductResponse;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    //====== Variables =====//

    private static final String TAG = "API_TEST";
    private RecyclerView rvCategories;

    //====== onCreate() =====//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Fixed shadowing: removed 'RecyclerView' type to assign to the class field
        rvCategories = findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );


        //====== API calls ======//
        fetchProducts();
        loadCategories();
    }

    //====== API  ======//

    // Fetch products from the API
    private void fetchProducts() {

        ApiService apiService =
                RetrofitClient.getClient().create(ApiService.class);

        Call<ProductResponse> call = apiService.getProducts();

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call,
                                   Response<ProductResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    // Added null check for getData()
                    if (response.body().getData() != null) {
                        int count = response.body().getData().size();
                        Log.d(TAG, "Products fetched successfully: " + count);
                    } else {
                        Log.w(TAG, "Products fetched but data list is null");
                    }
                } else {
                    Log.e(TAG, "Response failed");
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Log.e(TAG, "API Error: " + t.getMessage());
            }
        });
    }

    // Fetch categories from the API
    private void loadCategories() {

        ApiService apiService =
                RetrofitClient.getClient().create(ApiService.class);

        apiService.getCategories().enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call,
                                   Response<CategoryResponse> response) {

                if (response.isSuccessful() && response.body() != null) {
                    // Added null check for getData()
                    if (response.body().getData() != null) {
                        CategoryAdapter adapter =
                                new CategoryAdapter(MainActivity.this,
                                        response.body().getData());
                        rvCategories.setAdapter(adapter);
                    } else {
                        Log.w("CATEGORY_API", "Categories fetched but data list is null");
                    }
                }
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                Log.e("CATEGORY_API", t.getMessage());
            }
        });
    }

}
