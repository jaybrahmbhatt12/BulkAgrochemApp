package com.bulkagrochemapp;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.activity.OnBackPressedCallback;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.adapter.BigProductAdapter;
import com.bulkagrochemapp.adapter.CartAdapter;
import com.bulkagrochemapp.adapter.CouponAdapter;
import com.bulkagrochemapp.model.CartItem;
import com.bulkagrochemapp.model.CouponResponse;
import com.bulkagrochemapp.model.Product;
import com.bulkagrochemapp.model.ProductResponse;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;
import com.bulkagrochemapp.utils.CartManager;
import com.google.android.material.snackbar.Snackbar;

import androidx.recyclerview.widget.ItemTouchHelper;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//=========== Cart Activity ===========//

public class CartActivity extends BaseActivity implements CartManager.OnCartDataChangedListener {

    RecyclerView rvCart, rvRecommended, rvCoupons;
    View skeletonRecommended, skeletonCoupons;
    TextView txtTotal;
    TextView txtEmptyCart;
    ImageButton btnOrders;
    Button browseProductBtn, payBtn;
    ProgressBar progressBarCart;

    private boolean flag=false;

    LinearLayout payLinearLayout;
    
    // Summary views
    CardView cardCartTotals;
    TextView txtSummarySubtotal, txtSummaryTax, txtSummaryTotal;

    //=========== onCreate() ===========//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        rvCart = findViewById(R.id.rvCart);
        txtTotal = findViewById(R.id.txtTotal);
        txtEmptyCart = findViewById(R.id.txtEmptyCart);
        btnOrders = findViewById(R.id.btnOrders);
        browseProductBtn = findViewById(R.id.browseProductBtn);
        payBtn = findViewById(R.id.payBtn);
        payLinearLayout = findViewById(R.id.payLinearLayout);
        progressBarCart = findViewById(R.id.progressBarCart);

        rvRecommended = findViewById(R.id.rvRecommended);
        skeletonRecommended = findViewById(R.id.skeleton_recommended);
        rvCoupons = findViewById(R.id.rvCoupons);
        skeletonCoupons = findViewById(R.id.skeleton_coupons);
        
        // Initialize summary views
        cardCartTotals = findViewById(R.id.cardCartTotals);
        txtSummarySubtotal = findViewById(R.id.txtSummarySubtotal);
        txtSummaryTax = findViewById(R.id.txtSummaryTax);
        txtSummaryTotal = findViewById(R.id.txtSummaryTotal);

        rvCart.setLayoutManager(new LinearLayoutManager(this));

        Intent intent2 = getIntent();
        flag = intent2.getBooleanExtra("flag",false);

        
        // Use Horizontal LinearLayoutManager for Recommended Section
        rvRecommended.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvCoupons.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        if (browseProductBtn != null) {
            browseProductBtn.setOnClickListener(v -> {
                Intent intent = new Intent(CartActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }

        if (payBtn != null) {
            payBtn.setOnClickListener(v -> {
                Intent intent = new Intent(CartActivity.this, OrderSummaryActivity.class);
                startActivity(intent);
            });
        }

        CartAdapter adapter = new CartAdapter(
                this,
                CartManager.getCartItems(),
                () -> {
                    updateCartUI();
                }
        );

        rvCart.setAdapter(adapter);

        // Redirect to Orders Activity
        if (btnOrders != null) {
            btnOrders.setOnClickListener(v -> {
                Intent intent = new Intent(CartActivity.this, MyOrdersActivity.class);
                startActivity(intent);
            });
        }

        ItemTouchHelper.SimpleCallback swipeCallback =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(
                            RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder,
                            RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(
                            RecyclerView.ViewHolder viewHolder,
                            int direction) {

                        int position = viewHolder.getAdapterPosition();
                        List<CartItem> items = CartManager.getCartItems();
                        CartItem deletedItem = items.get(position);
                        int deletedPosition = position;

                        // Remove item
                        items.remove(position);
                        rvCart.getAdapter().notifyItemRemoved(position);
                        updateCartUI();

                        // Show Snackbar for Undo
                        Snackbar.make(rvCart, "Item removed from cart", 5000)
                                .setAction("UNDO", v -> {
                                    items.add(deletedPosition, deletedItem);
                                    rvCart.getAdapter().notifyItemInserted(deletedPosition);
                                    updateCartUI();
                                }).show();
                    }
                };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvCart);

        loadRecommendedProducts();
        loadCoupons();

        // Register listener for real-time updates when data is synced
        CartManager.addDataChangedListener(this);

        // Initial refresh
        updateCartUI();

        // Ensure data is fresh
        if (CartManager.getCartItems().isEmpty()) {
            fetchCartAndProducts();
        } else {
            // Even if not empty, trigger a sync to get latest details if they were "Loading..."
            syncCurrentCart();
        }

        getOnBackPressedDispatcher().addCallback(this, new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                if(flag==true)
                {
                    Intent intent = new Intent(CartActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right);
                    finish();
                }
                else{
                    finish();
                }
            }
        });
    }

    private void syncCurrentCart() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    CartManager.syncWithProducts(response.body().getData());
                }
            }
            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {}
        });
    }

    private void fetchCartAndProducts() {
        if (progressBarCart != null) progressBarCart.setVisibility(View.VISIBLE);

        CartManager.fetchCartFromDatabase(this, () -> {
            ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
            apiService.getProducts().enqueue(new Callback<ProductResponse>() {
                @Override
                public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                    if (progressBarCart != null) progressBarCart.setVisibility(View.GONE);
                    if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                        CartManager.syncWithProducts(response.body().getData());
                    }
                }

                @Override
                public void onFailure(Call<ProductResponse> call, Throwable t) {
                    if (progressBarCart != null) progressBarCart.setVisibility(View.GONE);
                }
            });
        });
    }

    @Override
    public void onDataChanged() {
        runOnUiThread(() -> {
            if (rvCart != null && rvCart.getAdapter() != null) {
                rvCart.getAdapter().notifyDataSetChanged();
            }
            updateCartUI();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        CartManager.removeDataChangedListener(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (rvCart != null && rvCart.getAdapter() != null) {
            rvCart.getAdapter().notifyDataSetChanged();
        }
        // Refresh recommended adapter to sync wishlist icon state
        if (rvRecommended != null && rvRecommended.getAdapter() != null) {
            rvRecommended.getAdapter().notifyDataSetChanged();
        }
        updateCartUI();
    }

    private void loadRecommendedProducts() {
        if (skeletonRecommended != null) {
            skeletonRecommended.setVisibility(View.VISIBLE);
            Animation pulseAnim = AnimationUtils.loadAnimation(this, R.anim.pulse);
            skeletonRecommended.startAnimation(pulseAnim);
        }
        if (rvRecommended != null) rvRecommended.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (skeletonRecommended != null) {
                    skeletonRecommended.clearAnimation();
                    skeletonRecommended.setVisibility(View.GONE);
                }
                if (rvRecommended != null) rvRecommended.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Product> allProducts = response.body().getData();
                    List<Product> recommendedList = new ArrayList<>();
                    for (Product product : allProducts) {
                        if (product.getIsBig() >= 1) {
                            recommendedList.add(product);
                        }
                    }

                    rvRecommended.setAdapter(new BigProductAdapter(CartActivity.this, recommendedList, R.layout.item_product_horizontal_cart));
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                if (skeletonRecommended != null) {
                    skeletonRecommended.clearAnimation();
                    skeletonRecommended.setVisibility(View.GONE);
                }
                Log.e("CART_API", "Error loading recommendations: " + t.getMessage());
            }
        });
    }

    private void loadCoupons() {
        if (skeletonCoupons != null) {
            skeletonCoupons.setVisibility(View.VISIBLE);
            Animation pulseAnim = AnimationUtils.loadAnimation(this, R.anim.pulse);
            skeletonCoupons.startAnimation(pulseAnim);
        }
        if (rvCoupons != null) rvCoupons.setVisibility(View.GONE);

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getCoupons().enqueue(new Callback<CouponResponse>() {
            @Override
            public void onResponse(Call<CouponResponse> call, Response<CouponResponse> response) {
                if (skeletonCoupons != null) {
                    skeletonCoupons.clearAnimation();
                    skeletonCoupons.setVisibility(View.GONE);
                }
                if (rvCoupons != null) rvCoupons.setVisibility(View.VISIBLE);

                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    rvCoupons.setAdapter(new CouponAdapter(CartActivity.this, response.body().getData()));
                }
            }

            @Override
            public void onFailure(Call<CouponResponse> call, Throwable t) {
                if (skeletonCoupons != null) {
                    skeletonCoupons.clearAnimation();
                    skeletonCoupons.setVisibility(View.GONE);
                }
                Log.e("COUPON_API", "Error loading coupons: " + t.getMessage());
            }
        });
    }

    private void updateCartUI() {
        if (txtEmptyCart == null || browseProductBtn == null) return;

        double subtotal = CartManager.getSubtotalBeforeTax();
        double tax = CartManager.getTotalTaxAmount();
        double total = CartManager.getTotalAmount();

        if (CartManager.getCartItems().isEmpty()) {
            txtEmptyCart.setVisibility(View.VISIBLE);
            browseProductBtn.setVisibility(View.VISIBLE);
            rvCart.setVisibility(View.GONE);
            payLinearLayout.setVisibility(View.GONE);
            if (cardCartTotals != null) cardCartTotals.setVisibility(View.GONE);
            
            txtTotal.setText("₹0.00");
            if (txtSummarySubtotal != null) txtSummarySubtotal.setText("₹0.00");
            if (txtSummaryTax != null) txtSummaryTax.setText("₹0.00");
            if (txtSummaryTotal != null) txtSummaryTotal.setText("₹0.00");
        } else {
            txtEmptyCart.setVisibility(View.GONE);
            browseProductBtn.setVisibility(View.GONE);
            rvCart.setVisibility(View.VISIBLE);
            payLinearLayout.setVisibility(View.VISIBLE);
            if (cardCartTotals != null) cardCartTotals.setVisibility(View.VISIBLE);
            
            txtTotal.setText(String.format(Locale.getDefault(), "₹%.2f", total));
            
            if (txtSummarySubtotal != null) 
                txtSummarySubtotal.setText(String.format(Locale.getDefault(), "₹%.2f", subtotal));
            if (txtSummaryTax != null) 
                txtSummaryTax.setText(String.format(Locale.getDefault(), "₹%.2f", tax));
            if (txtSummaryTotal != null) 
                txtSummaryTotal.setText(String.format(Locale.getDefault(), "₹%.2f", total));
        }
    }
}
