package com.bulkagrochemapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.adapter.CategoryAdapter;
import com.bulkagrochemapp.adapter.MostPopularProductAdapter;
import com.bulkagrochemapp.adapter.SearchSuggestionAdapter;
import com.bulkagrochemapp.adapter.TrendingProductAdapter;
import com.bulkagrochemapp.model.CategoryResponse;
import com.bulkagrochemapp.model.Product;
import com.bulkagrochemapp.model.ProductResponse;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SearchSuggestionAdapter.OnSuggestionClickListener {

    //====== Variables =====//

    private static final String TAG = "API_TEST";
    private RecyclerView rvCategories, rvTrending, rvPopular, rvSearchResults, rvHotSale;
    private EditText searchEditText;
    private ImageView ivProfile;
    private SearchSuggestionAdapter suggestionAdapter;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> searchResults = new ArrayList<>();
    private FloatingActionButton fabWhatsapp;

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

        rvPopular = findViewById(R.id.rvMostPopular);
        rvPopular.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        rvHotSale = findViewById(R.id.rvHotSale);
        rvHotSale.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );


        rvSearchResults = findViewById(R.id.rvSearchResults);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));

        searchEditText = findViewById(R.id.searchEditText);
        fabWhatsapp = findViewById(R.id.fabWhatsapp);
        ivProfile = findViewById(R.id.ivProfile);

        ivProfile.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        fabWhatsapp.setOnClickListener(v -> {
            String phoneNumber = "+919512017161";
            String message = "I need bulkagrochem product details.";
            String url = "https://api.whatsapp.com/send?phone=" + phoneNumber + "&text=" + message;

            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "Whatsapp is not installed on this device", Toast.LENGTH_SHORT).show();
            }
        });

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                searchProducts(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (searchResults.size() == 1) {
                    onSuggestionClick(searchResults.get(0));
                }
                return true;
            }
            return false;
        });


        //====== API calls ======//
        loadProducts(); // Load all products (for Popular)
        loadTrendingProducts(); // Load trending products from dedicated endpoint
        loadCategories();
    }

    private void searchProducts(String query) {
        if (query.isEmpty()) {
            rvSearchResults.setVisibility(View.GONE);
            return;
        }

        searchResults.clear();
        for (Product product : allProducts) {
            if (product.getName().toLowerCase().contains(query.toLowerCase())) {
                searchResults.add(product);
            }
        }

        if (searchResults.isEmpty()) {
            rvSearchResults.setVisibility(View.GONE);
        } else {
            suggestionAdapter = new SearchSuggestionAdapter(this, searchResults, this);
            rvSearchResults.setAdapter(suggestionAdapter);
            rvSearchResults.setVisibility(View.VISIBLE);
        }
    }

    //====== API  ======//

    // Fetch all products and filter for Popular
    private void loadProducts() {

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        Call<ProductResponse> call = apiService.getProducts();

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call,
                                   Response<ProductResponse> response) {

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    allProducts = response.body().getData();
                    Log.d(TAG, "Total products fetched: " + allProducts.size());

                    // Filter for Popular Products
                    List<Product> popularList = new ArrayList<>();
                    List<Product> hotSaleList = new ArrayList<>();
                    for (Product product : allProducts) {
                        // Check if popular flag is set
                        if (product.getIsPopular() >= 1) {
                            popularList.add(product);
                        }

                        // 🔥 Hot Sale
                        if (product.getIsHot() >= 1) {
                            hotSaleList.add(product);
                        }
                    }
                    // If no popular products found, maybe show all? No, stick to filtering.
                    MostPopularProductAdapter popularAdapter = new MostPopularProductAdapter(MainActivity.this, popularList);
                    rvPopular.setAdapter(popularAdapter);
                    Log.d(TAG, "Popular products count: " + popularList.size());

                    // Hot Sale Adapter (REUSE SAME ADAPTER + SAME LAYOUT)
                    MostPopularProductAdapter hotSaleAdapter =
                            new MostPopularProductAdapter(MainActivity.this, hotSaleList);
                    rvHotSale.setAdapter(hotSaleAdapter);
                    Log.d(TAG, "Hot Sale count: " + hotSaleList.size());

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

    // Fetch Trending products
    private void loadTrendingProducts() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ProductResponse> call = apiService.getTrendingProducts();

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Product> trendingList = response.body().getData();
                    TrendingProductAdapter trendingAdapter = new TrendingProductAdapter(MainActivity.this, trendingList);
                    rvTrending.setAdapter(trendingAdapter);
                    Log.d(TAG, "Trending products fetched from API: " + trendingList.size());
                } else {
                    Log.e(TAG, "Trending products response failed");
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Log.e(TAG, "Trending API Error: " + t.getMessage());
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

    @Override
    public void onSuggestionClick(Product product) {
        Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
        intent.putExtra("product_id", product.getId());
        intent.putExtra("product_name", product.getName());
        intent.putExtra("product_price", product.getPrice());
        intent.putExtra("product_stock", product.getStocktype());
        intent.putExtra("minimum_qty", product.getMinimumQty());
        intent.putExtra("product_image", product.getImage());
        intent.putExtra("product_description", product.getDescription());
        intent.putExtra("product_details", product.getDetails());
        intent.putExtra("product_original_price", product.getOriginalPrice());
        intent.putExtra("product_discount_percent", product.getDiscountPercent());
        startActivity(intent);
    }
}
