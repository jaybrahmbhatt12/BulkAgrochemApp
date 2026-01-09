package com.bulkagrochemapp;

import android.graphics.Paint;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.bulkagrochemapp.model.CartItem;
import com.bulkagrochemapp.model.Product;
import com.bulkagrochemapp.model.ProductResponse;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;
import com.bulkagrochemapp.utils.CartManager;
import com.bulkagrochemapp.utils.HtmlUtil;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";

    ImageView imgProduct;
    TextView txtName, txtPrice, txtStock, txtQty, txtDetails, txtDescription, txtOriginalPrice, txtDiscount, txtReviews;
    TextView btnPlus, btnMinus;
    View layoutQtySelector; 
    Button btnAddToCart, btnReturnToCart;
    RatingBar ratingBar;
    TableLayout tablePriceRange;
    View skeletonDescription, skeletonTable, skeletonPrice, skeletonDiscount;
    
    // Primary Shimmers
    View shimmerTable1, shimmerTable2, shimmerTable3, shimmerDesc1, shimmerDesc2, shimmerDesc3, shimmerPrice, shimmerDiscount;
    // Secondary Shimmers (for overlapping effect)
    View shimmerTable1_2, shimmerTable2_2, shimmerTable3_2, shimmerDesc1_2, shimmerDesc2_2, shimmerDesc3_2;

    int quantity = 0;
    int initialQuantity = 0;
    private Product product;
    private final Random random = new Random();

    int productId;
    String intentName, intentImage, intentStocktype;
    double intentPrice;
    int intentMinQty;
    boolean isComingFromCart = false;
    
    private Call<ProductResponse> currentCall;
    private final Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Bind views
        imgProduct = findViewById(R.id.imgProduct);
        txtName = findViewById(R.id.txtProductName);
        txtPrice = findViewById(R.id.txtProductPrice);
        txtStock = findViewById(R.id.txtStock);
        txtDescription = findViewById(R.id.txtDescription);
        txtDetails = findViewById(R.id.txtDetails);
        txtQty = findViewById(R.id.txtQty);
        btnPlus = findViewById(R.id.btnPlus);
        btnMinus = findViewById(R.id.btnMinus);
        layoutQtySelector = (View) btnPlus.getParent(); 
        btnAddToCart = findViewById(R.id.btnAddToCart);
        btnReturnToCart = findViewById(R.id.btnReturnToCart);
        txtOriginalPrice = findViewById(R.id.txtOriginalPrice);
        txtDiscount = findViewById(R.id.txtDiscount);
        txtReviews = findViewById(R.id.txtReviews);
        ratingBar = findViewById(R.id.ratingBar);
        tablePriceRange = findViewById(R.id.tablePriceRange);
        
        skeletonDescription = findViewById(R.id.skeletonDescription);
        skeletonTable = findViewById(R.id.skeletonTable);
        skeletonPrice = findViewById(R.id.skeletonPrice);
        skeletonDiscount = findViewById(R.id.skeletonDiscount);

        // Bind Primary Shimmer views
        shimmerTable1 = findViewById(R.id.shimmerTable1);
        shimmerTable2 = findViewById(R.id.shimmerTable2);
        shimmerTable3 = findViewById(R.id.shimmerTable3);
        shimmerDesc1 = findViewById(R.id.shimmerDesc1);
        shimmerDesc2 = findViewById(R.id.shimmerDesc2);
        shimmerDesc3 = findViewById(R.id.shimmerDesc3);
        shimmerPrice = findViewById(R.id.shimmerPrice);
        shimmerDiscount = findViewById(R.id.shimmerDiscount);

        // Bind Secondary Shimmer views
        shimmerTable1_2 = findViewById(R.id.shimmerTable1_2);
        shimmerTable2_2 = findViewById(R.id.shimmerTable2_2);
        shimmerTable3_2 = findViewById(R.id.shimmerTable3_2);
        shimmerDesc1_2 = findViewById(R.id.shimmerDesc1_2);
        shimmerDesc2_2 = findViewById(R.id.shimmerDesc2_2);
        shimmerDesc3_2 = findViewById(R.id.shimmerDesc3_2);

        // Get intent data
        product = (Product) getIntent().getSerializableExtra("product");
        productId = getIntent().getIntExtra("product_id", 0);
        intentName = getIntent().getStringExtra("product_name");
        intentImage = getIntent().getStringExtra("product_image");
        intentPrice = getIntent().getDoubleExtra("product_price", 0);
        intentStocktype = getIntent().getStringExtra("product_stock");
        intentMinQty = getIntent().getIntExtra("minimum_qty", 0);

        isComingFromCart = getIntent().getBooleanExtra("is_from_cart", false);

        if (isComingFromCart) {
            layoutQtySelector.setVisibility(View.GONE);
            btnAddToCart.setVisibility(View.GONE);
            btnReturnToCart.setVisibility(View.VISIBLE);
        }

        btnReturnToCart.setOnClickListener(v -> finish());

        if (product != null) {
            productId = product.getId();
            updateUI(product);
        } else {
            txtName.setText(intentName != null ? intentName : "");
            txtPrice.setText("₹" + intentPrice + "/" + (intentStocktype != null ? intentStocktype : ""));
            txtStock.setText("In Stock");
            
            initialQuantity = intentMinQty;
            quantity = initialQuantity;
            txtQty.setText(String.valueOf(quantity));

            if (intentImage != null && !intentImage.isEmpty()) {
                Picasso.get().load(intentImage).placeholder(R.drawable.ic_launcher_foreground).into(imgProduct);
            }
            
            showSkeletons(true);
        }

        int reviewsCount = random.nextInt(91) + 10;
        txtReviews.setText(reviewsCount + " Reviews");

        if (productId != 0) {
            fetchFullProductDetails(productId);
        }

        btnPlus.setOnClickListener(v -> {
            if (initialQuantity > 0) {
                quantity += initialQuantity;
                txtQty.setText(String.valueOf(quantity));
            }
        });

        btnMinus.setOnClickListener(v -> {
            if (initialQuantity > 0 && quantity > initialQuantity) {
                quantity -= initialQuantity;
                txtQty.setText(String.valueOf(quantity));
            }
        });

        btnAddToCart.setOnClickListener(v -> {
            CartItem cartItem;
            if (product != null) {
                cartItem = new CartItem(product.getId(), product.getName(), product.getImage(), product.getPrice(), product.getMinimumQty(), product.getStocktype());
            } else {
                cartItem = new CartItem(productId, intentName, intentImage, intentPrice, intentMinQty, intentStocktype);
            }
            cartItem.setQuantity(quantity);
            CartManager.addToCart(cartItem);
            Toast.makeText(this, "Added to cart", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (currentCall != null) {
            currentCall.cancel();
        }
        handler.removeCallbacksAndMessages(null);
    }

    private void showSkeletons(boolean show) {
        runOnUiThread(() -> {
            if (skeletonDescription != null) skeletonDescription.setVisibility(show ? View.VISIBLE : View.GONE);
            if (skeletonTable != null) skeletonTable.setVisibility(show ? View.VISIBLE : View.GONE);
            if (skeletonPrice != null) skeletonPrice.setVisibility(show ? View.VISIBLE : View.GONE);
            if (skeletonDiscount != null) skeletonDiscount.setVisibility(show ? View.VISIBLE : View.GONE);

            if (show) {
                Animation shimmerAnim1 = AnimationUtils.loadAnimation(this, R.anim.shimmer_translate);
                Animation shimmerAnim2 = AnimationUtils.loadAnimation(this, R.anim.shimmer_translate);
                
                // Start primary shimmers
                startShimmer(shimmerTable1, shimmerAnim1);
                startShimmer(shimmerTable2, shimmerAnim1);
                startShimmer(shimmerTable3, shimmerAnim1);
                startShimmer(shimmerDesc1, shimmerAnim1);
                startShimmer(shimmerDesc2, shimmerAnim1);
                startShimmer(shimmerDesc3, shimmerAnim1);
                startShimmer(shimmerPrice, shimmerAnim1);
                startShimmer(shimmerDiscount, shimmerAnim1);

                // Start secondary shimmers with an offset to create "infinite wave" effect
                handler.postDelayed(() -> {
                    startShimmer(shimmerTable1_2, shimmerAnim2);
                    startShimmer(shimmerTable2_2, shimmerAnim2);
                    startShimmer(shimmerTable3_2, shimmerAnim2);
                    startShimmer(shimmerDesc1_2, shimmerAnim2);
                    startShimmer(shimmerDesc2_2, shimmerAnim2);
                    startShimmer(shimmerDesc3_2, shimmerAnim2);
                }, 600); // 600ms offset (half of duration)
                
            } else {
                stopShimmer(shimmerTable1); stopShimmer(shimmerTable1_2);
                stopShimmer(shimmerTable2); stopShimmer(shimmerTable2_2);
                stopShimmer(shimmerTable3); stopShimmer(shimmerTable3_2);
                stopShimmer(shimmerDesc1); stopShimmer(shimmerDesc1_2);
                stopShimmer(shimmerDesc2); stopShimmer(shimmerDesc2_2);
                stopShimmer(shimmerDesc3); stopShimmer(shimmerDesc3_2);
                stopShimmer(shimmerPrice);
                stopShimmer(shimmerDiscount);
            }
        });
    }

    private void startShimmer(View view, Animation anim) {
        if (view != null) {
            view.setVisibility(View.VISIBLE);
            view.startAnimation(anim);
        }
    }

    private void stopShimmer(View view) {
        if (view != null) {
            view.clearAnimation();
            view.setVisibility(View.GONE);
        }
    }

    private void fetchFullProductDetails(int id) {
        if (id == 0) {
            showSkeletons(false);
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        currentCall = apiService.getProductDetails(id);

        currentCall.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    product = response.body().getData().get(0);
                    showSkeletons(false);
                    updateUI(product);
                } else {
                    handler.postDelayed(() -> fetchFallbackProducts(id), 500);
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                if (!call.isCanceled()) {
                    fetchFallbackProducts(id);
                }
            }
        });
    }

    private void fetchFallbackProducts(int id) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ProductResponse> fallbackCall = apiService.getProducts();

        fallbackCall.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                showSkeletons(false);
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null) {
                    List<Product> allProducts = response.body().getData();
                    for (Product p : allProducts) {
                        if (p.getId() == id) {
                            product = p;
                            updateUI(p);
                            break;
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                showSkeletons(false);
                Log.e(TAG, "Fallback Error: " + t.getMessage());
            }
        });
    }

    private void updateUI(Product p) {
        if (p == null || isFinishing() || isDestroyed()) return;

        runOnUiThread(() -> {
            if (skeletonPrice != null) skeletonPrice.setVisibility(View.GONE);
            if (skeletonDiscount != null) skeletonDiscount.setVisibility(View.GONE);

            txtName.setText(p.getName());
            txtPrice.setText("₹" + p.getPrice() + "/" + (p.getStocktype() != null ? p.getStocktype() : ""));
            txtStock.setText("In Stock");
            
            initialQuantity = p.getMinimumQty();
            if (quantity == 0 || quantity < initialQuantity) {
                 quantity = initialQuantity;
            }
            txtQty.setText(String.valueOf(quantity));

            if (p.getOriginalPrice() > p.getPrice()) {
                txtOriginalPrice.setText("₹" + String.format("%.2f", p.getOriginalPrice()));
                txtOriginalPrice.setPaintFlags(txtOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                txtOriginalPrice.setVisibility(View.VISIBLE);
            } else {
                txtOriginalPrice.setVisibility(View.GONE);
            }

            if (p.getDiscountPercent() > 0) {
                txtDiscount.setText(p.getDiscountPercent() + "% OFF");
                txtDiscount.setVisibility(View.VISIBLE);
            } else {
                txtDiscount.setVisibility(View.GONE);
            }

            if (p.getDescription() != null && !p.getDescription().isEmpty()) {
                txtDescription.setText(HtmlUtil.fromHtml(p.getDescription()));
                txtDescription.setVisibility(View.VISIBLE);
            } else {
                txtDescription.setVisibility(View.GONE);
            }

            if (p.getDetails() != null && !p.getDetails().isEmpty()) {
                txtDetails.setText(HtmlUtil.fromHtml(p.getDetails()));
                txtDetails.setVisibility(View.VISIBLE);
            } else {
                txtDetails.setVisibility(View.GONE);
            }

            Picasso.get()
                    .load(p.getImage())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(imgProduct);

            setupWholesaleTable(p);
        });
    }

    private void setupWholesaleTable(Product p) {
        if (tablePriceRange == null) return;

        int childCount = tablePriceRange.getChildCount();
        if (childCount > 1) {
            tablePriceRange.removeViews(1, childCount - 1);
        }

        if (p.getWholeSellQty() != null && p.getWholeSellDiscount() != null) {
            String[] qtyArray = p.getWholeSellQty().split(",");
            String[] priceArray = p.getWholeSellDiscount().split(",");
            int length = Math.min(qtyArray.length, priceArray.length);

            for (int i = 0; i < length; i++) {
                String q = qtyArray[i].trim();
                String pr = priceArray[i].trim();
                if (q.isEmpty() || pr.isEmpty()) continue;

                TableRow row = new TableRow(this);
                row.setPadding(8, 8, 8, 8);

                TextView qtyTv = new TextView(this);
                qtyTv.setText(q);
                qtyTv.setPadding(16, 16, 16, 16);
                qtyTv.setTextColor(ContextCompat.getColor(this, R.color.black));
                qtyTv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
                qtyTv.setLayoutParams(params);

                TextView priceTv = new TextView(this);
                priceTv.setText("₹ " + pr);
                priceTv.setPadding(16, 16, 16, 16);
                priceTv.setTextColor(ContextCompat.getColor(this, R.color.black));
                priceTv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
                priceTv.setLayoutParams(params);

                row.addView(qtyTv);
                row.addView(priceTv);
                tablePriceRange.addView(row);
            }
        }
    }
}
