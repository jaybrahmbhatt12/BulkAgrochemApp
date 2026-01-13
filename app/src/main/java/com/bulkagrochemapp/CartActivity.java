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
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.adapter.CartAdapter;
import com.bulkagrochemapp.adapter.TopSellingProductAdapter;
import com.bulkagrochemapp.adapter.TrendingProductAdapter;
import com.bulkagrochemapp.model.CartItem;
import com.bulkagrochemapp.model.Product;
import com.bulkagrochemapp.model.ProductResponse;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;
import com.bulkagrochemapp.utils.CartManager;
import com.google.android.material.snackbar.Snackbar;

import androidx.recyclerview.widget.ItemTouchHelper;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

//=========== Cart Activity ===========//

public class CartActivity extends BaseActivity {

    RecyclerView rvCart, rvTrending, rvTopSelling;
    View skeletonTrending, skeletonTopSelling;
    TextView txtTotal;
    TextView txtEmptyCart;
    ImageButton btnHome;
    Button browseProductBtn;

    LinearLayout payLinearLayout;



    //=========== onCreate() ===========//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        rvCart = findViewById(R.id.rvCart);
        txtTotal = findViewById(R.id.txtTotal);
        txtEmptyCart = findViewById(R.id.txtEmptyCart);
        btnHome = findViewById(R.id.btnHome);
        browseProductBtn = findViewById(R.id.browseProductBtn);
        payLinearLayout = findViewById(R.id.payLinearLayout);

        rvTrending = findViewById(R.id.rvTrending);
        rvTopSelling = findViewById(R.id.rvTopSelling);
        skeletonTrending = findViewById(R.id.skeleton_trending);
        skeletonTopSelling = findViewById(R.id.skeleton_topselling);

        rvCart.setLayoutManager(new LinearLayoutManager(this));
        rvTrending.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        rvTopSelling.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));

        if (browseProductBtn != null) {
            browseProductBtn.setOnClickListener(v -> {
                Intent intent = new Intent(CartActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            });
        }

        CartAdapter adapter = new CartAdapter(
                this,
                CartManager.getCartItems(),
                () -> {
                    txtTotal.setText("₹" + CartManager.getTotalAmount());
                    updateCartUI();
                }
        );

        rvCart.setAdapter(adapter);
        txtTotal.setText("₹" + CartManager.getTotalAmount());

        // Redirect to Home Screen with smooth slide transition
        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                Intent intent = new Intent(CartActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                // Standard slide transition: Enter from left, exit to right
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
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
                        txtTotal.setText("₹" + CartManager.getTotalAmount());
                        updateCartUI();

                        // Show Snackbar for Undo
                        Snackbar.make(rvCart, "Item removed from cart", 5000)
                                .setAction("UNDO", v -> {
                                    items.add(deletedPosition, deletedItem);
                                    rvCart.getAdapter().notifyItemInserted(deletedPosition);
                                    txtTotal.setText("₹" + CartManager.getTotalAmount());
                                    updateCartUI();
                                }).show();
                    }
                };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvCart);

        showSkeletons(true);
        loadSuggestions();
        updateCartUI();

    }

    @Override
    protected void onResume() {
        super.onResume();
        // Refresh cart list and UI when returning from ProductDetailActivity
        if (rvCart != null && rvCart.getAdapter() != null) {
            rvCart.getAdapter().notifyDataSetChanged();
        }
        updateCartUI();
    }

    private void showSkeletons(boolean show) {
        int skeletonVis = show ? View.VISIBLE : View.GONE;
        int rvVis = show ? View.GONE : View.VISIBLE;

        skeletonTrending.setVisibility(skeletonVis);
        skeletonTopSelling.setVisibility(skeletonVis);

        rvTrending.setVisibility(rvVis);
        rvTopSelling.setVisibility(rvVis);

        if (show) {
            Animation pulseAnim = AnimationUtils.loadAnimation(this, R.anim.pulse);
            skeletonTrending.startAnimation(pulseAnim);
            skeletonTopSelling.startAnimation(pulseAnim);
        } else {
            skeletonTrending.clearAnimation();
            skeletonTopSelling.clearAnimation();
        }
    }

    private void loadSuggestions() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getProducts().enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                showSkeletons(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Product> allProducts = response.body().getData();
                    List<Product> trendingList = new ArrayList<>();
                    List<Product> topSellingList = new ArrayList<>();

                    for (Product product : allProducts) {
                        if (product.getIsTrending() >= 1) {
                            trendingList.add(product);
                        }
                        if (product.getIsTop() >= 1) {
                            topSellingList.add(product);
                        }
                    }

                    // Pass the smaller layout ID only for the Cart view
                    rvTrending.setAdapter(new TrendingProductAdapter(CartActivity.this, trendingList, R.layout.item_product_horizontal_cart));
                    rvTopSelling.setAdapter(new TopSellingProductAdapter(CartActivity.this, topSellingList, R.layout.item_product_horizontal_cart));
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                showSkeletons(false);
                Log.e("CART_API", "Error loading suggestions: " + t.getMessage());
            }
        });
    }

    private void updateCartUI() {
        if (txtEmptyCart == null || browseProductBtn == null) return;

        if (CartManager.getCartItems().isEmpty()) {
            txtEmptyCart.setVisibility(View.VISIBLE);
            browseProductBtn.setVisibility(View.VISIBLE);
            rvCart.setVisibility(View.GONE);
            txtTotal.setText("₹0");
            payLinearLayout.setVisibility(View.GONE);
        } else {
            txtEmptyCart.setVisibility(View.GONE);
            browseProductBtn.setVisibility(View.GONE);
            rvCart.setVisibility(View.VISIBLE);
            txtTotal.setText("₹" + CartManager.getTotalAmount());
            payLinearLayout.setVisibility(View.VISIBLE);
        }
    }
}
