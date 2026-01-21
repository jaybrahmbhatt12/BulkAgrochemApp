package com.bulkagrochemapp;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.inputmethod.EditorInfo;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.core.widget.NestedScrollView;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.bulkagrochemapp.adapter.BannerAdapter;
import com.bulkagrochemapp.adapter.BigProductAdapter;
import com.bulkagrochemapp.adapter.CategoryAdapter;
import com.bulkagrochemapp.adapter.CouponAdapter;
import com.bulkagrochemapp.adapter.HotProductAdapter;
import com.bulkagrochemapp.adapter.LatestProductAdapter;
import com.bulkagrochemapp.adapter.MostPopularProductAdapter;
import com.bulkagrochemapp.adapter.SearchSuggestionAdapter;
import com.bulkagrochemapp.adapter.TopSellingProductAdapter;
import com.bulkagrochemapp.adapter.TrendingProductAdapter;
import com.bulkagrochemapp.model.Category;
import com.bulkagrochemapp.model.CategoryResponse;
import com.bulkagrochemapp.model.CouponResponse;
import com.bulkagrochemapp.model.Product;
import com.bulkagrochemapp.model.ProductResponse;
import com.bulkagrochemapp.model.Slider;
import com.bulkagrochemapp.model.SliderResponse;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.navigation.NavigationView;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends BaseActivity implements SearchSuggestionAdapter.OnSuggestionClickListener, NavigationView.OnNavigationItemSelectedListener {

    private static final String TAG = "API_TEST";
    private RecyclerView rvCategories, rvTrending, rvPopular, rvSearchResults, rvHotSale, rvLatestProduct, rvBigProduct, rvTopSelling, rvCoupons;
    private View skeletonCategories, skeletonTrending, skeletonPopular, skeletonHotSale, skeletonLatest, skeletonBig, skeletonTopSelling, skeletonBanner;
    private EditText searchEditText;
    private ViewPager2 bannerViewPager;
    private Handler bannerHandler = new Handler(Looper.getMainLooper());
    private Runnable bannerRunnable;
    private List<Slider> sliderList = new ArrayList<>();
    private SearchSuggestionAdapter suggestionAdapter;
    private List<Product> allProducts = new ArrayList<>();
    private List<Product> searchResults = new ArrayList<>();
    private FloatingActionButton fabWhatsapp, GoUp;
    private DrawerLayout drawerLayout;
    private NavigationView navigationView;
    private NestedScrollView nestedScrollView;
    private TextView categoriesTitle, trendingProductsTitle, mostPopularTitle, hotSaleTitle, txtCouponsTitle, txtLatestTitle, txtBigTitle, txtTopSellingTitle;
    private List<Category> categoryList = new ArrayList<>();
    private List<MenuItem> categoryMenuItems = new ArrayList<>();
    private boolean areCategoriesVisible = false;

    private ImageButton btnWishlist;
    private View btnPlayVideo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        nestedScrollView = findViewById(R.id.main);

        // Ensure icons show in their original colors
        if (navigationView != null) {
            navigationView.setItemIconTintList(null);
        }

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

        View headerView = navigationView.getHeaderView(0);
        LinearLayout profileContainer = headerView.findViewById(R.id.profileContainer);
        if (profileContainer != null) {
            profileContainer.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, ProfileActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
            });
        }

        // Initialize Skeletons
        skeletonBanner = findViewById(R.id.skeleton_banner);
        skeletonCategories = findViewById(R.id.skeleton_categories);
        skeletonTrending = findViewById(R.id.skeleton_trending);
        skeletonPopular = findViewById(R.id.skeleton_popular);
        skeletonHotSale = findViewById(R.id.skeleton_hotsale);
        skeletonLatest = findViewById(R.id.skeleton_latest);
        skeletonBig = findViewById(R.id.skeleton_big);
        skeletonTopSelling = findViewById(R.id.skeleton_topselling);

        bannerViewPager = findViewById(R.id.bannerViewPager);
        bannerViewPager.setOffscreenPageLimit(7);

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

        rvCoupons = findViewById(R.id.rvCoupons);
        rvCoupons.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));


        rvSearchResults = findViewById(R.id.rvSearchResults);
        rvSearchResults.setLayoutManager(new LinearLayoutManager(this));

        searchEditText = findViewById(R.id.searchEditText);
        fabWhatsapp = findViewById(R.id.fabWhatsapp);
        GoUp = findViewById(R.id.Go_up);

        categoriesTitle = findViewById(R.id.categories_title);
        trendingProductsTitle = findViewById(R.id.trending_products_title);
        mostPopularTitle = findViewById(R.id.most_popular_title);
        hotSaleTitle = findViewById(R.id.hot_sale_title);
        txtCouponsTitle = findViewById(R.id.txtCouponsTitle);
        txtLatestTitle = findViewById(R.id.txtLatestProduct);
        txtBigTitle = findViewById(R.id.txtBigProduct);
        txtTopSellingTitle = findViewById(R.id.txtTopSelling);

        if (GoUp != null) {
            GoUp.setOnClickListener(v -> {
                if (nestedScrollView != null) {
                    nestedScrollView.smoothScrollTo(0, 0);
                }
            });
        }

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

        showAllSkeletons(true);

        //====== API calls ======//
        loadBanners(); // Fetch from PHP/MySQL
        loadProducts(); // Load all products and filter
        loadCategories();
        loadTrendingProducts();
        loadCoupons();

        btnWishlist = findViewById(R.id.btnWishlist);

        if(btnWishlist != null) {
            btnWishlist.setOnClickListener(v -> {
                Intent intent = new Intent(MainActivity.this, WishlistActivity.class);
                startActivity(intent);
            });
        }

        btnPlayVideo = findViewById(R.id.btnPlayVideo);
        if (btnPlayVideo != null) {
            btnPlayVideo.setOnClickListener(v -> showVideoPopup());
        }
    }

    private void showVideoPopup() {
        Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_video_player);
        dialog.getWindow().setLayout(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);

        WebView webView = dialog.findViewById(R.id.webViewVideo);
        Button btnClose = dialog.findViewById(R.id.btnCloseVideo);

        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setMediaPlaybackRequiresUserGesture(false);

        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());

        // YouTube embed URL
        String videoId = "ok6XEDZfLHg";
        String embedUrl = "https://www.youtube.com/embed/" + videoId;
        String html = "<html><body style='margin:0;padding:0;'><iframe width=\"100%\" height=\"100%\" src=\"" + embedUrl + "\" frameborder=\"0\" allowfullscreen></iframe></body></html>";

        // Use loadDataWithBaseURL to fix "Error 153" by providing a valid origin
        webView.loadDataWithBaseURL("https://youtu.be/ok6XEDZfLHg", html, "text/html", "utf-8", null);

        btnClose.setOnClickListener(v -> {
            webView.loadUrl("about:blank");
            dialog.dismiss();
        });

        dialog.show();
    }

    private void loadBanners() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getSliders().enqueue(new Callback<SliderResponse>() {
            @Override
            public void onResponse(Call<SliderResponse> call, Response<SliderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    sliderList = response.body().getSliders();
                    if (sliderList != null && !sliderList.isEmpty()) {
                        skeletonBanner.setVisibility(View.GONE);
                        skeletonBanner.clearAnimation();
                        bannerViewPager.setVisibility(View.VISIBLE);

                        BannerAdapter bannerAdapter = new BannerAdapter(MainActivity.this, sliderList);
                        bannerViewPager.setAdapter(bannerAdapter);
                        setupAutoSlider();
                    }
                }
            }

            @Override
            public void onFailure(Call<SliderResponse> call, Throwable t) {
                skeletonBanner.setVisibility(View.GONE);
                Log.e(TAG, "Banner API Failure: " + t.getMessage());
            }
        });
    }

    private void setupAutoSlider() {
        if (bannerRunnable != null) bannerHandler.removeCallbacks(bannerRunnable);
        bannerRunnable = new Runnable() {
            @Override
            public void run() {
                int nextItem = bannerViewPager.getCurrentItem() + 1;
                if (nextItem >= sliderList.size()) {
                    nextItem = 0;
                }
                bannerViewPager.setCurrentItem(nextItem, true);
                bannerHandler.postDelayed(this, 4000);
            }
        };
        bannerHandler.postDelayed(bannerRunnable, 4000);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bannerHandler != null && bannerRunnable != null) {
            bannerHandler.removeCallbacks(bannerRunnable);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshAdapters();
    }

    private void refreshAdapters() {
        if (rvTrending != null && rvTrending.getAdapter() != null) rvTrending.getAdapter().notifyDataSetChanged();
        if (rvPopular != null && rvPopular.getAdapter() != null) rvPopular.getAdapter().notifyDataSetChanged();
        if (rvHotSale != null && rvHotSale.getAdapter() != null) rvHotSale.getAdapter().notifyDataSetChanged();
        if (rvLatestProduct != null && rvLatestProduct.getAdapter() != null) rvLatestProduct.getAdapter().notifyDataSetChanged();
        if (rvBigProduct != null && rvBigProduct.getAdapter() != null) rvBigProduct.getAdapter().notifyDataSetChanged();
        if (rvTopSelling != null && rvTopSelling.getAdapter() != null) rvTopSelling.getAdapter().notifyDataSetChanged();
        if (rvCoupons != null && rvCoupons.getAdapter() != null) rvCoupons.getAdapter().notifyDataSetChanged();
    }

    private void showAllSkeletons(boolean show) {
        int skeletonVis = show ? View.VISIBLE : View.GONE;
        int rvVis = show ? View.GONE : View.VISIBLE;

        skeletonBanner.setVisibility(skeletonVis);
        skeletonCategories.setVisibility(skeletonVis);
        skeletonTrending.setVisibility(skeletonVis);
        skeletonPopular.setVisibility(skeletonVis);
        skeletonHotSale.setVisibility(skeletonVis);
        skeletonLatest.setVisibility(skeletonVis);
        skeletonBig.setVisibility(skeletonVis);
        skeletonTopSelling.setVisibility(skeletonVis);

        // Don't hide RVs here, visibility is managed in load methods

        if (show) {
            Animation pulseAnim = AnimationUtils.loadAnimation(this, R.anim.pulse);
            skeletonBanner.startAnimation(pulseAnim);
            skeletonCategories.startAnimation(pulseAnim);
            skeletonTrending.startAnimation(pulseAnim);
            skeletonPopular.startAnimation(pulseAnim);
            skeletonHotSale.startAnimation(pulseAnim);
            skeletonLatest.startAnimation(pulseAnim);
            skeletonBig.startAnimation(pulseAnim);
            skeletonTopSelling.startAnimation(pulseAnim);

            // Add shimmer to banner specifically
            View shimmerView = skeletonBanner.findViewById(R.id.skeleton_banner).findViewWithTag("shimmer");
            if (shimmerView != null) {
                Animation shimmerAnim = AnimationUtils.loadAnimation(this, R.anim.shimmer_translate);
                shimmerView.startAnimation(shimmerAnim);
            }
        } else {
            skeletonBanner.clearAnimation();
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

    // API //

    // Fetch all products and filter for Popular and Trending
    private void loadProducts() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                // IMPORTANT: Hide ALL product skeletons when products are fetched
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

                    List<Product> popularList = new ArrayList<>(), hotList = new ArrayList<>(), latestList = new ArrayList<>(), bigList = new ArrayList<>(), topList = new ArrayList<>(), trendList = new ArrayList<>();
                    for (Product p : allProducts) {
                        if (p.getIsPopular() >= 1) popularList.add(p);
                        if (p.getIsHot() >= 1) hotList.add(p);
                        if (p.getIsLatest() >= 1) latestList.add(p);
                        if (p.getIsBig() >= 1) bigList.add(p);
                        if (p.getIsTop() >= 1) topList.add(p);
                        if (p.getIsTrending() >= 1) trendList.add(p);
                    }
                    rvPopular.setAdapter(new MostPopularProductAdapter(MainActivity.this, popularList));
                    rvHotSale.setAdapter(new HotProductAdapter(MainActivity.this, hotList));
                    rvLatestProduct.setAdapter(new LatestProductAdapter(MainActivity.this, latestList));
                    rvBigProduct.setAdapter(new BigProductAdapter(MainActivity.this, bigList));
                    rvTopSelling.setAdapter(new TopSellingProductAdapter(MainActivity.this, topList));
                }
            }
            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                // Ensure skeletons hide even on failure
                skeletonPopular.setVisibility(View.GONE); skeletonHotSale.setVisibility(View.GONE);
                skeletonLatest.setVisibility(View.GONE); skeletonBig.setVisibility(View.GONE);
                skeletonTopSelling.setVisibility(View.GONE);
            }
        });
    }

    private void loadTrendingProducts() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getTrendingProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                skeletonTrending.setVisibility(View.GONE);
                skeletonTrending.clearAnimation();
                rvTrending.setVisibility(View.VISIBLE);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Product> trendList = new ArrayList<>();
                    for (Product p : response.body().getData()) if (p.getIsTrending() >= 1) trendList.add(p);
                    rvTrending.setAdapter(new TrendingProductAdapter(MainActivity.this, trendList));
                }
            }
            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                skeletonTrending.setVisibility(View.GONE);
            }
        });
    }

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
                }
            }
            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                skeletonCategories.setVisibility(View.GONE);
            }
        });
    }

    private void loadCoupons() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getCoupons().enqueue(new Callback<CouponResponse>() {
            @Override
            public void onResponse(Call<CouponResponse> call, Response<CouponResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List coupons = response.body().getData();
                    if (coupons == null || coupons.isEmpty()) {
                        if (rvCoupons != null) rvCoupons.setVisibility(View.GONE);
                        if (txtCouponsTitle != null) txtCouponsTitle.setVisibility(View.GONE);
                    } else {
                        if (rvCoupons != null) {
                            rvCoupons.setAdapter(new CouponAdapter(MainActivity.this, coupons));
                            rvCoupons.setVisibility(View.VISIBLE);
                        }
                        if (txtCouponsTitle != null) txtCouponsTitle.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<CouponResponse> call, Throwable t) {
                Log.e("COUPON_API", "Error loading coupons: " + t.getMessage());
                if (rvCoupons != null) rvCoupons.setVisibility(View.GONE);
                if (txtCouponsTitle != null) txtCouponsTitle.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onSuggestionClick(Product product) {
        Intent intent = new Intent(this, ProductDetailActivity.class);
        intent.putExtra("product", product);
        startActivity(intent);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_categories_toggle) {
            toggleCategoriesInMenu();
            return true;
        }

        View targetView = null;

        if (id == R.id.nav_trending_products) {
            targetView = trendingProductsTitle;
        } else if (id == R.id.nav_most_popular) {
            targetView = mostPopularTitle;
        } else if (id == R.id.nav_hot_sale) {
            targetView = hotSaleTitle;
        } else if (id == R.id.nav_latest_product) {
            targetView = txtLatestTitle;
        } else if (id == R.id.nav_big_product) {
            targetView = txtBigTitle;
        } else if (id == R.id.nav_top_selling) {
            targetView = txtTopSellingTitle;
        }

        if (targetView != null) {
            final View viewToScrollTo = targetView;
            drawerLayout.closeDrawers();
            new Handler().postDelayed(() -> {
                if (nestedScrollView != null) {
                    // Slower smooth scroll using ObjectAnimator for a better experience
                    ObjectAnimator.ofInt(nestedScrollView, "scrollY", viewToScrollTo.getTop())
                            .setDuration(1500) // 1.5 seconds for a smooth, slow scroll
                            .start();
                }
            }, 300); // Small delay to allow drawer to close
            return true;
        }

        // Check if it's a dynamic category item
        for (Category cat : categoryList) {
            if (id == cat.getId()) {
                Intent intent = new Intent(this, ProductListActivity.class);
                intent.putExtra("category_id", cat.getId());
                intent.putExtra("category_name", cat.getName());
                startActivity(intent);
                drawerLayout.closeDrawers();
                return true;
            }
        }

        drawerLayout.closeDrawers();
        return false;
    }

    private void toggleCategoriesInMenu() {
        Menu menu = navigationView.getMenu();
        if (areCategoriesVisible) {
            // Hide categories
            for (MenuItem item : categoryMenuItems) {
                menu.removeItem(item.getItemId());
            }
            categoryMenuItems.clear();
        } else {
            // Show categories
            int order = 101;
            for (Category cat : categoryList) {
                MenuItem item = menu.add(R.id.nav_group, cat.getId(), order++, cat.getName());
                // Removed icon from sub-categories as requested
                categoryMenuItems.add(item);
            }
        }
        areCategoriesVisible = !areCategoriesVisible;
    }
}
