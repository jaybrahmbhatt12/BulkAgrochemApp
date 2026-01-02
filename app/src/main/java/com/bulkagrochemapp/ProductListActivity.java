package com.bulkagrochemapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.adapter.ProductAdapter;
import com.bulkagrochemapp.adapter.SearchSuggestionAdapter;
import com.bulkagrochemapp.model.Product;
import com.bulkagrochemapp.model.ProductResponse;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductListActivity extends AppCompatActivity implements ProductAdapter.OnProductClickListener, SearchSuggestionAdapter.OnSuggestionClickListener {

    RecyclerView rvProducts, rvProductSearchResults;
    TextView txtTitle;
    SearchView searchView;

    int categoryId;
    ProductAdapter adapter;
    SearchSuggestionAdapter suggestionAdapter;
    List<Product> productList = new ArrayList<>();
    private List<Product> filteredProductList = new ArrayList<>();

    private static final String TAG = "PRODUCT_LIST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        Log.d(TAG, "ProductListActivity OPENED");

        rvProducts = findViewById(R.id.rvProducts);
        rvProductSearchResults = findViewById(R.id.rvProductSearchResults);
        txtTitle = findViewById(R.id.txtTitle);
        searchView = findViewById(R.id.searchView);

        rvProducts.setLayoutManager(new GridLayoutManager(this, 1));
        rvProductSearchResults.setLayoutManager(new LinearLayoutManager(this));

        adapter = new ProductAdapter(this, productList, this);
        rvProducts.setAdapter(adapter);

        suggestionAdapter = new SearchSuggestionAdapter(this, filteredProductList, this);
        rvProductSearchResults.setAdapter(suggestionAdapter);

        categoryId = getIntent().getIntExtra("category_id", 0);
        String categoryName = getIntent().getStringExtra("category_name");

        Log.d(TAG, "Category ID = " + categoryId);
        Log.d(TAG, "Category Name = " + categoryName);

        if (categoryId == 0) {
            Toast.makeText(this, "Invalid category", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtTitle.setText(categoryName != null ? categoryName : "Products");

        loadProductsByCategory();

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                if (filteredProductList.size() == 1) {
                    onSuggestionClick(filteredProductList.get(0));
                }
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                filter(newText);
                return true;
            }
        });
    }

    private void loadProductsByCategory() {

        Log.d(TAG, "Calling API for categoryId=" + categoryId);

        ApiService apiService =
                RetrofitClient.getClient().create(ApiService.class);

        apiService.getProductsByCategory(categoryId)
                .enqueue(new Callback<ProductResponse>() {

                    @Override
                    public void onResponse(Call<ProductResponse> call,
                                           Response<ProductResponse> response) {

                        Log.d(TAG, "HTTP Code = " + response.code());

                        if (!response.isSuccessful()) {
                            Log.e(TAG, "API failed: HTTP " + response.code());
                            Toast.makeText(
                                    ProductListActivity.this,
                                    "Server error",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        ProductResponse body = response.body();

                        if (body == null) {
                            Log.e(TAG, "Response body is NULL");
                            return;
                        }

                        if (body.getData() == null || body.getData().isEmpty()) {
                            Log.w(TAG, "No products found");
                            Toast.makeText(
                                    ProductListActivity.this,
                                    "No products found in this category",
                                    Toast.LENGTH_SHORT
                            ).show();
                            return;
                        }

                        productList.clear();
                        productList.addAll(body.getData());
                        adapter.notifyDataSetChanged();

                        Log.d(TAG, "Products loaded: " + productList.size());
                    }

                    @Override
                    public void onFailure(Call<ProductResponse> call, Throwable t) {
                        Log.e(TAG, "API ERROR", t);
                        Toast.makeText(
                                ProductListActivity.this,
                                "Network error",
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void filter(String text) {
        filteredProductList.clear();
        if (text.isEmpty()) {
            rvProductSearchResults.setVisibility(View.GONE);
            rvProducts.setVisibility(View.VISIBLE);
        } else {
            rvProductSearchResults.setVisibility(View.VISIBLE);
            rvProducts.setVisibility(View.GONE);
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(text.toLowerCase())) {
                    filteredProductList.add(product);
                }
            }
        }
        suggestionAdapter.notifyDataSetChanged();
    }

    @Override
    public void onProductClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }

    @Override
    public void onSuggestionClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }
}
