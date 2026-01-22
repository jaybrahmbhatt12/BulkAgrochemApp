package com.bulkagrochemapp;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
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

public class ProductListActivity extends BaseActivity implements ProductAdapter.OnProductClickListener, SearchSuggestionAdapter.OnSuggestionClickListener {

    RecyclerView rvProducts, rvProductSearchResults;
    TextView txtTitle;
    EditText searchEditText;
    ImageButton btnClearSearch;
    View searchContainer, skeletonLayout, searchOverlay, searchResultContainer;

    int categoryId;
    ProductAdapter adapter;
    SearchSuggestionAdapter suggestionAdapter;
    List<Product> productList = new ArrayList<>();
    private List<Product> searchResults = new ArrayList<>();

    private static final String TAG = "PRODUCT_LIST";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_list);

        Log.d(TAG, "ProductListActivity OPENED");

        rvProducts = findViewById(R.id.rvProducts);
        rvProductSearchResults = findViewById(R.id.rvProductSearchResults);
        txtTitle = findViewById(R.id.txtTitle);
        searchEditText = findViewById(R.id.searchEditText);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        searchContainer = findViewById(R.id.search_container);
        skeletonLayout = findViewById(R.id.skeleton_layout);
        searchOverlay = findViewById(R.id.searchOverlay);
        searchResultContainer = findViewById(R.id.searchResultContainer);

        rvProducts.setLayoutManager(new GridLayoutManager(this, 1));
        rvProductSearchResults.setLayoutManager(new LinearLayoutManager(this));
        rvProductSearchResults.setNestedScrollingEnabled(true);

        // Attach empty adapter first
        adapter = new ProductAdapter(this, productList, this);
        rvProducts.setAdapter(adapter);

        suggestionAdapter = new SearchSuggestionAdapter(this, searchResults, this);
        rvProductSearchResults.setAdapter(suggestionAdapter);

        // Get data from intent
        categoryId = getIntent().getIntExtra("category_id", 0);
        String categoryName = getIntent().getStringExtra("category_name");

        boolean isSale = getIntent().getBooleanExtra("is_sale", false);

        if (isSale) {
            if (searchContainer != null) {
                searchContainer.setVisibility(View.GONE);
            }
        }

        if (categoryId == 0 && !isSale) {
            Toast.makeText(this, "Invalid category", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        txtTitle.setText(categoryName != null ? categoryName : "Products");

        if (isSale) {
            loadSaleProducts();
        } else {
            loadProductsByCategory();
        }

        initSearch();

        // Setup Back Press Handler
        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if (searchResultContainer != null && searchResultContainer.getVisibility() == View.VISIBLE) {
                    hideSearchOverlay();
                } else {
                    setEnabled(false);
                    getOnBackPressedDispatcher().onBackPressed();
                }
            }
        });
    }

    private void initSearch() {
        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (btnClearSearch != null) {
                    btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
                }
                filter(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        searchEditText.setOnClickListener(v -> {
            String query = searchEditText.getText().toString();
            if (!query.isEmpty()) {
                showSearchOverlay(query);
            }
        });

        searchEditText.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                String query = searchEditText.getText().toString();
                if (!query.isEmpty()) {
                    showSearchOverlay(query);
                }
            }
        });

        if (btnClearSearch != null) {
            btnClearSearch.setOnClickListener(v -> {
                searchEditText.setText("");
                hideSearchOverlay();
            });
        }

        if (searchOverlay != null) {
            searchOverlay.setOnClickListener(v -> hideSearchOverlay());
        }

        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                if (!searchResults.isEmpty()) {
                    onSuggestionClick(searchResults.get(0));
                }
                hideKeyboard();
                return true;
            }
            return false;
        });
    }

    private void showSearchOverlay(String query) {
        filter(query);
        if (searchOverlay != null) searchOverlay.setVisibility(View.VISIBLE);
    }

    private void hideSearchOverlay() {
        if (searchResultContainer != null) searchResultContainer.setVisibility(View.GONE);
        if (searchOverlay != null) searchOverlay.setVisibility(View.GONE);
        hideKeyboard();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            if (imm != null) {
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    private void showSkeleton(boolean show) {
        if (skeletonLayout != null) {
            if (show) {
                skeletonLayout.setVisibility(View.VISIBLE);
                rvProducts.setVisibility(View.GONE);
                Animation pulse = AnimationUtils.loadAnimation(this, R.anim.pulse);
                skeletonLayout.startAnimation(pulse);
            } else {
                skeletonLayout.clearAnimation();
                skeletonLayout.setVisibility(View.GONE);
                rvProducts.setVisibility(View.VISIBLE);
            }
        }
    }

    private void loadSaleProducts() {
        showSkeleton(true);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                showSkeleton(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    productList.clear();
                    for (Product p : response.body().getData()) {
                        if (p.getIsHot() >= 1) {
                            productList.add(p);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                showSkeleton(false);
                Toast.makeText(ProductListActivity.this, "Error loading sales", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadProductsByCategory() {
        showSkeleton(true);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getProductsByCategory(categoryId)
                .enqueue(new Callback<ProductResponse>() {
                    @Override
                    public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                        showSkeleton(false);
                        if (!response.isSuccessful()) {
                            Toast.makeText(ProductListActivity.this, "Server error", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        ProductResponse body = response.body();
                        if (body == null || body.getData() == null || body.getData().isEmpty()) {
                            Toast.makeText(ProductListActivity.this, "No products found", Toast.LENGTH_SHORT).show();
                            return;
                        }
                        productList.clear();
                        productList.addAll(body.getData());
                        adapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onFailure(Call<ProductResponse> call, Throwable t) {
                        showSkeleton(false);
                        Toast.makeText(ProductListActivity.this, "Network error", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void filter(String text) {
        searchResults.clear();
        if (text.isEmpty()) {
            if (searchResultContainer != null) searchResultContainer.setVisibility(View.GONE);
        } else {
            for (Product product : productList) {
                if (product.getName().toLowerCase().contains(text.toLowerCase())) {
                    searchResults.add(product);
                }
            }
            suggestionAdapter.notifyDataSetChanged();
            if (!searchResults.isEmpty() && searchResultContainer != null) {
                searchResultContainer.setVisibility(View.VISIBLE);
            }
        }
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
