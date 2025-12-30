package com.bulkagrochemapp;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.adapter.ProductAdapter;
import com.bulkagrochemapp.model.ProductResponse;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    int categoryId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        recyclerView = findViewById(R.id.rvProducts);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        // 🔹 GET CATEGORY ID SAFELY
        if (getIntent() != null && getIntent().hasExtra("category_id")) {
            categoryId = getIntent().getIntExtra("category_id", -1);
        }

        if (categoryId == -1) {
            Toast.makeText(this, "Category not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        fetchProductsByCategory(categoryId);
    }

    private void fetchProductsByCategory(int categoryId) {

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Call<ProductResponse> call =
                apiService.getProductsByCategory(categoryId);

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call,
                                   Response<ProductResponse> response) {

                if (response.isSuccessful()
                        && response.body() != null
                        && response.body().getData() != null) {

                    ProductAdapter adapter =
                            new ProductAdapter(ProductListActivity.this,
                                    response.body().getData());

                    recyclerView.setAdapter(adapter);

                } else {
                    Toast.makeText(ProductListActivity.this,
                            "No products found", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Log.e("PRODUCT_API", t.getMessage());
            }
        });
    }
}
