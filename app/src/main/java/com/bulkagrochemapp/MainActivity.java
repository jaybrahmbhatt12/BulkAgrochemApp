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
import com.bulkagrochemapp.adapter.MostPopularProductAdapter;
import com.bulkagrochemapp.adapter.TrendingProductAdapter;
import com.bulkagrochemapp.model.CategoryResponse;
import com.bulkagrochemapp.model.Product;
import com.bulkagrochemapp.model.ProductResponse;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    //====== Variables =====//

    private static final String TAG = "API_TEST";
    private RecyclerView rvCategories, rvTrending, rvPopular;

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

        rvCategories = findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        rvTrending = findViewById(R.id.rvTrending);
        rvTrending.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        rvPopular = findViewById(R.id.rvPopular);
        rvPopular.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );


        //====== API calls ======//
        loadProducts();
        loadCategories();
    }

    //====== API  ======//

    // Fetch all products and filter them into categories
    private void loadProducts() {

        ApiService apiService =
                RetrofitClient.getClient().create(ApiService.class);

        Call<ProductResponse> call = apiService.getProducts();

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call,
                                   Response<ProductResponse> response) {

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Product> productList = response.body().getData();
                    Log.d(TAG, "Total products fetched: " + productList.size());

                    // Filter for Trending Products
                    List<Product> trendingList = new ArrayList<>();
                    for (Product product : productList) {
                        if (product.getIsTrending() == 1) {
                            trendingList.add(product);
                        }
                    }
                    TrendingProductAdapter trendingAdapter = new TrendingProductAdapter(MainActivity.this, trendingList);
                    rvTrending.setAdapter(trendingAdapter);
                    Log.d(TAG, "Trending products count: " + trendingList.size());

                    // Filter for Popular Products
                    List<Product> popularList = new ArrayList<>();
                    for (Product product : productList) {
                        if (product.getIsPopular() == 1) {
                            popularList.add(product);
                        }
                    }
                    MostPopularProductAdapter popularAdapter = new MostPopularProductAdapter(MainActivity.this, popularList);
                    rvPopular.setAdapter(popularAdapter);
                    Log.d(TAG, "Popular products count: " + popularList.size());

                } else {
                    Log.e(TAG, "Product response failed or data is null");
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

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    CategoryAdapter adapter =
                            new CategoryAdapter(MainActivity.this, response.body().getData());
                    rvCategories.setAdapter(adapter);
                } else {
                    Log.w("CATEGORY_API", "Categories response failed or data is null");
                }
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                Log.e("CATEGORY_API", t.getMessage());
            }
        });
    }

}
