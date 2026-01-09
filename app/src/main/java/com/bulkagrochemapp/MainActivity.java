package com.bulkagrochemapp;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.adapter.BigProductAdapter;
import com.bulkagrochemapp.adapter.CategoryAdapter;
import com.bulkagrochemapp.adapter.HotProductAdapter;
import com.bulkagrochemapp.adapter.LatestProductAdapter;
import com.bulkagrochemapp.adapter.MostPopularProductAdapter;
import com.bulkagrochemapp.adapter.SearchSuggestionAdapter;
import com.bulkagrochemapp.adapter.TopSellingProductAdapter;
import com.bulkagrochemapp.adapter.TrendingProductAdapter;
import com.bulkagrochemapp.model.Category;
import com.bulkagrochemapp.model.CategoryResponse;
import com.bulkagrochemapp.model.Product;
import com.bulkagrochemapp.model.ProductResponse;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity implements SearchSuggestionAdapter.OnSuggestionClickListener, NavigationView.OnNavigationItemSelectedListener {

    //====== Variables =====//

    private static final String TAG = "API_TEST";
    private RecyclerView rvCategories, rvTrending, rvPopular, rvSearchResults, rvHotSale, rvLatestProduct, rvBigProduct, rvTopSelling;
    private View skeletonCategories, skeletonTrending, skeletonPopular, skeletonHotSale, skeletonLatest, skeletonBig, skeletonTopSelling;
    private EditText searchEditText;
    private ImageView ivProfile;
    private SearchSuggestionAdapter suggestionAdapter;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> searchResults = new ArrayList<>();
    private FloatingActionButton fabWhatsapp;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NestedScrollView nestedScrollView;
    private TextView categoriesTitle, trendingProductsTitle, mostPopularTitle, hotSaleTitle;
    private List<Category> categoryList = new ArrayList<>();
    private List<MenuItem> categoryMenuItems = new ArrayList<>();
    private boolean areCategoriesVisible = false;

    private ImageButton cartBtn;

    //====== onCreate() =====//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Make status bar transparent for edge-to-edge
        Window window = getWindow();
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Remove default title
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        nestedScrollView = findViewById(R.id.main);

        // Apply padding to AppBarLayout/Toolbar to avoid overlap
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.appBarLayout), (v, insets) -> {
             int top = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
             v.setPadding(0, top, 0, 0);
             return insets;
        });

        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.black));
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();
        
        navigationView.setNavigationItemSelectedListener(this);
        
        ViewCompat.setOnApplyWindowInsetsListener(navigationView, (v, insets) -> {
            return insets;
        });
        
        // Handle nav header profile click
        View headerView = navigationView.getHeaderView(0);
        LinearLayout profileContainer = headerView.findViewById(R.id.profileContainer);
        if (profileContainer != null) {
            profileContainer.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                startActivity(intent);
            });
        }

        // Initialize Skeletons
        skeletonCategories = findViewById(R.id.skeleton_categories);
        skeletonTrending = findViewById(R.id.skeleton_trending);
        skeletonPopular = findViewById(R.id.skeleton_popular);
        skeletonHotSale = findViewById(R.id.skeleton_hotsale);
        skeletonLatest = findViewById(R.id.skeleton_latest);
        skeletonBig = findViewById(R.id.skeleton_big);
        skeletonTopSelling = findViewById(R.id.skeleton_topselling);

        rvCategories = findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        rvTrending = findViewById(R.id.rvTrending);
        rvTrending.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        rvPopular = findViewById(R.id.rvMostPopular);
        rvPopular.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        rvHotSale = findViewById(R.id.rvHotSale);
        rvHotSale.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        rvLatestProduct = findViewById(R.id.rvLatestProduct);
        rvLatestProduct.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        rvBigProduct = findViewById(R.id.rvBigProduct);
        rvBigProduct.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        rvTopSelling = findViewById(R.id.rvTopSelling);
        rvTopSelling.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


        rvSearchResults = findViewById(R.id.rvSearchResults);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));

        searchEditText = findViewById(R.id.searchEditText);
        fabWhatsapp = findViewById(R.id.fabWhatsapp);

        categoriesTitle = findViewById(R.id.categories_title);
        trendingProductsTitle = findViewById(R.id.trending_products_title);
        mostPopularTitle = findViewById(R.id.most_popular_title);
        hotSaleTitle = findViewById(R.id.hot_sale_title);

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

        // Show all skeletons and hide RecyclerViews at start
        showAllSkeletons(true);

        //====== API calls ======//
        loadProducts(); 
        loadTrendingProducts(); 
        loadCategories();

        cartBtn = findViewById(R.id.cartbtn);

        if(cartBtn != null) {
            cartBtn.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, CartActivity.class);
                startActivity(intent);
            });
        }
    }

    private void showAllSkeletons(boolean show) {
        int skeletonVis = show ? View.VISIBLE : View.GONE;
        int rvVis = show ? View.GONE : View.VISIBLE;

        skeletonCategories.setVisibility(skeletonVis);
        skeletonTrending.setVisibility(skeletonVis);
        skeletonPopular.setVisibility(skeletonVis);
        skeletonHotSale.setVisibility(skeletonVis);
        skeletonLatest.setVisibility(skeletonVis);
        skeletonBig.setVisibility(skeletonVis);
        skeletonTopSelling.setVisibility(skeletonVis);

        rvCategories.setVisibility(rvVis);
        rvTrending.setVisibility(rvVis);
        rvPopular.setVisibility(rvVis);
        rvHotSale.setVisibility(rvVis);
        rvLatestProduct.setVisibility(rvVis);
        rvBigProduct.setVisibility(rvVis);
        rvTopSelling.setVisibility(rvVis);

        if (show) {
            Animation pulseAnim = AnimationUtils.loadAnimation(this, R.anim.pulse);
            skeletonCategories.startAnimation(pulseAnim);
            skeletonTrending.startAnimation(pulseAnim);
            skeletonPopular.startAnimation(pulseAnim);
            skeletonHotSale.startAnimation(pulseAnim);
            skeletonLatest.startAnimation(pulseAnim);
            skeletonBig.startAnimation(pulseAnim);
            skeletonTopSelling.startAnimation(pulseAnim);
        } else {
            skeletonCategories.clearAnimation();
            skeletonTrending.clearAnimation();
            skeletonPopular.clearAnimation();
            skeletonHotSale.clearAnimation();
            skeletonLatest.clearAnimation();
            skeletonBig.clearAnimation();
            skeletonTopSelling.clearAnimation();
        }
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
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                // Hide specific skeletons when products are loaded
                skeletonPopular.setVisibility(View.GONE); skeletonPopular.clearAnimation();
                skeletonHotSale.setVisibility(View.GONE); skeletonHotSale.clearAnimation();
                skeletonLatest.setVisibility(View.GONE); skeletonLatest.clearAnimation();
                skeletonBig.setVisibility(View.GONE); skeletonBig.clearAnimation();
                skeletonTopSelling.setVisibility(View.GONE); skeletonTopSelling.clearAnimation();

                rvPopular.setVisibility(View.VISIBLE);
                rvHotSale.setVisibility(View.VISIBLE);
                rvLatestProduct.setVisibility(View.VISIBLE);
                rvBigProduct.setVisibility(View.VISIBLE);
                rvTopSelling.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    allProducts = response.body().getData();
                    
                    List<Product> popularList = new ArrayList<>();
                    List<Product> hotSaleList = new ArrayList<>();
                    List<Product> latestList = new ArrayList<>();
                    List<Product> bigProductList = new ArrayList<>();
                    List<Product> topSellingList = new ArrayList<>();

                    for (Product product : allProducts) {
                        if (product.getIsPopular() >= 1) popularList.add(product);
                        if (product.getIsHot() >= 1) hotSaleList.add(product);
                        if (product.getIsLatest() >= 1) latestList.add(product);
                        if (product.getIsBig() >= 1) bigProductList.add(product);
                        if (product.getIsTop() >= 1) topSellingList.add(product);
                    }

                    rvPopular.setAdapter(new MostPopularProductAdapter(MainActivity.this, popularList));
                    rvHotSale.setAdapter(new HotProductAdapter(MainActivity.this, hotSaleList));
                    rvLatestProduct.setAdapter(new LatestProductAdapter(MainActivity.this, latestList));
                    rvBigProduct.setAdapter(new BigProductAdapter(MainActivity.this, bigProductList));
                    rvTopSelling.setAdapter(new TopSellingProductAdapter(MainActivity.this, topSellingList));
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                showAllSkeletons(false); // Safety
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
                skeletonTrending.setVisibility(View.GONE);
                skeletonTrending.clearAnimation();
                rvTrending.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Product> allTrending = response.body().getData();
                    List<Product> filteredTrending = new ArrayList<>();
                    for (Product product : allTrending) {
                        if (product.getIsTrending() >= 1) filteredTrending.add(product);
                    }
                    rvTrending.setAdapter(new TrendingProductAdapter(MainActivity.this, filteredTrending));
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                skeletonTrending.setVisibility(View.GONE);
                Log.e(TAG, "Trending API Error: " + t.getMessage());
            }
        });
    }

    // Fetch categories from the API
    private void loadCategories() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getCategories().enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                skeletonCategories.setVisibility(View.GONE);
                skeletonCategories.clearAnimation();
                rvCategories.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    categoryList = response.body().getData();
                    rvCategories.setAdapter(new CategoryAdapter(MainActivity.this, categoryList));

                    Menu menu = navigationView.getMenu();
                    for (int i = 0; i < categoryList.size(); i++) {
                        Category category = categoryList.get(i);
                        String indentedTitle = "    " + category.getName();
                        MenuItem menuItem = menu.add(R.id.nav_group, category.getId(), 101 + i, indentedTitle);
                        menuItem.setVisible(false);
                        categoryMenuItems.add(menuItem);
                    }
                }
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                skeletonCategories.setVisibility(View.GONE);
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
        intent.putExtra("product", product);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_categories_toggle) {
            areCategoriesVisible = !areCategoriesVisible;
            for (MenuItem menuItem : categoryMenuItems) {
                menuItem.setVisible(areCategoriesVisible);
            }
            return true; 
        }

        for (Category category : categoryList) {
            if (id == category.getId()) {
                Intent intent = new Intent(this, ProductListActivity.class);
                intent.putExtra("category_id", category.getId());
                intent.putExtra("category_name", category.getName());
                startActivity(intent);
                drawerLayout.closeDrawers();
                return true;
            }
        }

        TextView targetView = null;
        if (id == R.id.nav_trending_products) {
            targetView = trendingProductsTitle;
        } else if (id == R.id.nav_most_popular) {
            targetView = mostPopularTitle;
        } else if (id == R.id.nav_hot_sale) {
            targetView = hotSaleTitle;
        } else if (id == R.id.nav_latest_product) {
            targetView = findViewById(R.id.txtLatestProduct);
        } else if (id == R.id.nav_big_product) {
            targetView = findViewById(R.id.txtBigProduct);
        } else if (id == R.id.nav_top_selling) {
            targetView = findViewById(R.id.txtTopSelling);
        }
        
        if (targetView != null) {
            scrollToView(targetView);
            drawerLayout.closeDrawers();
            return true;
        }

        drawerLayout.closeDrawers();
        return true;
    }

    private void scrollToView(View view) {
        if (view != null) {
            nestedScrollView.post(() -> nestedScrollView.smoothScrollTo(0, view.getTop()));
        }
    }
}
