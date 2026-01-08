package com.bulkagrochemapp;

import android.graphics.Paint;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
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

import java.util.Random;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    private static final String TAG = "ProductDetailActivity";

    ImageView imgProduct;
    TextView txtName, txtPrice, txtStock, txtQty, txtDetails, txtDescription, txtOriginalPrice, txtDiscount, txtReviews;
    TextView btnPlus, btnMinus;
    Button btnAddToCart;
    RatingBar ratingBar;
    TableLayout tablePriceRange;

    int quantity = 0;
    int initialQuantity=0,minimum_qty_value=0;
    private Product product;
    private Random random = new Random();

    int productId;
    String name, image, stock, details, description , stocktype;
    double price;
    double originalPrice;
    int discountPercent;

    //========== onCreat() ===========//
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // 🔹 Bind views
        // for displaying data in product detail activity came trough category activity
        imgProduct = findViewById(R.id.imgProduct);
        txtName = findViewById(R.id.txtProductName);
        txtPrice = findViewById(R.id.txtProductPrice);
        txtStock = findViewById(R.id.txtStock);
        txtDescription = findViewById(R.id.txtDescription);
        txtDetails = findViewById(R.id.txtDetails);
        txtQty = findViewById(R.id.txtQty);
        btnPlus = findViewById(R.id.btnPlus);
        btnMinus = findViewById(R.id.btnMinus);
        btnAddToCart = findViewById(R.id.btnAddToCart);
        txtOriginalPrice = findViewById(R.id.txtOriginalPrice);
        txtDiscount = findViewById(R.id.txtDiscount);
        txtReviews = findViewById(R.id.txtReviews);
        ratingBar = findViewById(R.id.ratingBar);
        tablePriceRange = findViewById(R.id.tablePriceRange);

        // 🔹 Get intent data
        // for displaying data in product detail activity came trough search bar
        product = (Product) getIntent().getSerializableExtra("product");

        // for displaying data in product detail activity came trough category activity
        productId = getIntent().getIntExtra("product_id", 0);
        name = getIntent().getStringExtra("product_name");
        image = getIntent().getStringExtra("product_image");
        price = getIntent().getDoubleExtra("product_price", 0);
        stocktype = getIntent().getStringExtra("product_stock");
        minimum_qty_value = getIntent().getIntExtra("minimum_qty",0);
        stock = getIntent().getStringExtra("stock");
        description = getIntent().getStringExtra("product_description");
        details = getIntent().getStringExtra("product_details");
        originalPrice = getIntent().getDoubleExtra("product_original_price", 0);
        discountPercent = getIntent().getIntExtra("product_discount_percent", 0);

        // Generate random reviews count between 10 and 100
        int reviewsCount = random.nextInt(91) + 10;
        txtReviews.setText(reviewsCount + " Reviews");

        if (product != null) {
            // 🔹 Set data
            // for displaying data in product detail activity came trough search bar
            txtName.setText(product.getName());
            txtPrice.setText("₹" + product.getPrice() + "/"+product.getStocktype());
            txtStock.setText("In Stock"); // Assuming all products are in stock
            quantity = product.getMinimumQty();
            initialQuantity = quantity;
            txtQty.setText(String.valueOf(quantity));

            // Original Price and Discount
            if (product.getOriginalPrice() > product.getPrice()) {
                 txtOriginalPrice.setText("₹" + String.format("%.2f", product.getOriginalPrice()));
                 txtOriginalPrice.setPaintFlags(txtOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                 txtOriginalPrice.setVisibility(View.VISIBLE);
            } else {
                 txtOriginalPrice.setVisibility(View.GONE);
            }

            if (product.getDiscountPercent() > 0) {
                txtDiscount.setText(product.getDiscountPercent() + "% OFF");
                txtDiscount.setVisibility(View.VISIBLE);
            } else {
                txtDiscount.setVisibility(View.GONE);
            }

            if (product.getDetails() != null && !product.getDetails().isEmpty()) {
                txtDetails.setText(HtmlUtil.fromHtml(product.getDetails()));
                txtDetails.setVisibility(View.VISIBLE);
            } else {
                txtDetails.setVisibility(View.GONE);
            }

            Picasso.get()
                    .load(product.getImage())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(imgProduct);

            // Setup table if product data is already available
            setupWholesaleTable(product);
        }
        else {
            // for displaying data in product detail activity came trough category activity
            txtName.setText(name);
            txtPrice.setText("₹" + price + "/" + stocktype);
            txtStock.setText(stock != null ? stock : "In Stock");
            quantity = minimum_qty_value;
            initialQuantity = quantity;
            txtQty.setText(String.valueOf(quantity));

             // Original Price and Discount (from intent extras)
            if (originalPrice > price) {
                 txtOriginalPrice.setText("₹" + String.format("%.2f", originalPrice));
                 txtOriginalPrice.setPaintFlags(txtOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                 txtOriginalPrice.setVisibility(View.VISIBLE);
            } else {
                 txtOriginalPrice.setVisibility(View.GONE);
            }

            if (discountPercent > 0) {
                txtDiscount.setText(discountPercent + "% OFF");
                txtDiscount.setVisibility(View.VISIBLE);
            } else {
                txtDiscount.setVisibility(View.GONE);
            }

            if (details != null && !details.isEmpty()) {
                txtDetails.setText(HtmlUtil.fromHtml(details));
                txtDetails.setVisibility(View.VISIBLE);
            } else {
                txtDetails.setVisibility(View.GONE);
            }

            Picasso.get()
                    .load(image)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(imgProduct);
        }

        // Fetch full product details from API to ensure we have everything (e.g. if coming from Trending)
        if (product != null) {
            fetchProductDetails(product.getId());
        } else {
            fetchProductDetails(productId);
        }

        // Quantity +
        btnPlus.setOnClickListener(v -> {
            quantity+=initialQuantity;
            txtQty.setText(String.valueOf(quantity));
        });

        // 🔹 Quantity -
        btnMinus.setOnClickListener(v -> {
            if (quantity > initialQuantity) {
                quantity-=initialQuantity;
                txtQty.setText(String.valueOf(quantity));
            }
        });

        // 🔹 Add to cart
        btnAddToCart.setOnClickListener(v -> {

            if (product == null){//if no searchbar product clicked
                // for displaying data in product detail activity came trough category activity
                CartItem cartItem =
                        new CartItem(productId,name, image, price);

                for (int i = 1; i < quantity; i++) {
                    cartItem.increaseQty();
                }

                CartManager.addToCart(cartItem);
            }
            else {
                //if serched bar product clicked
                CartItem cartItem =
                        new CartItem(product.getId(), product.getName(), product.getImage(), product.getPrice());

                for (int i = 1; i < quantity; i++) {
                    cartItem.increaseQty();
                }

                CartManager.addToCart(cartItem);
            }

            Toast.makeText(
                    ProductDetailActivity.this,
                    "Added to cart",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }

    //========== fetchProductDetails() ===========//
    private void fetchProductDetails(int id) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ProductResponse> call = apiService.getProductDetails(id);

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    Product fetchedProduct = response.body().getData().get(0);

                    // If we didn't have a product object before, we can use this one
                    if (product == null) {
                        product = fetchedProduct;
                    }

                    // Update UI with fresh data
                    // Especially description and details which might be missing from the list item
                    if (fetchedProduct.getDescription() != null && !fetchedProduct.getDescription().isEmpty()) {
                        txtDescription.setText(HtmlUtil.fromHtml(fetchedProduct.getDescription()));
                        txtDescription.setVisibility(View.VISIBLE);
                    }

                    if (fetchedProduct.getDetails() != null && !fetchedProduct.getDetails().isEmpty()) {
                        txtDetails.setText(HtmlUtil.fromHtml(fetchedProduct.getDetails()));
                        txtDetails.setVisibility(View.VISIBLE);
                    }

                    // Also update price info if available
                    if (fetchedProduct.getOriginalPrice() > fetchedProduct.getPrice()) {
                        txtOriginalPrice.setText("₹" + String.format("%.2f", fetchedProduct.getOriginalPrice()));
                        txtOriginalPrice.setPaintFlags(txtOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                        txtOriginalPrice.setVisibility(View.VISIBLE);
                    }

                    if (fetchedProduct.getDiscountPercent() > 0) {
                        txtDiscount.setText(fetchedProduct.getDiscountPercent() + "% OFF");
                        txtDiscount.setVisibility(View.VISIBLE);
                    }

                    setupWholesaleTable(fetchedProduct);
                }

                if (product != null) {
                    Log.d("API_PRODUCT", "whole_sell_qty = " + product.getWholeSellQty());
                    Log.d("API_PRODUCT", "whole_sell_discount = " + product.getWholeSellDiscount());
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                // Ignore error, stick with Intent data
                Log.e(TAG, "fetchProductDetails failed: " + t.getMessage());
            }
        });
    }

    //========== setupWholesaleTable() ===========//
    private void setupWholesaleTable(Product product) {

        if (tablePriceRange == null) {
             tablePriceRange = findViewById(R.id.tablePriceRange);
        }

        if (tablePriceRange == null) return;

        // Clear previous rows except header (index 0)
        int childCount = tablePriceRange.getChildCount();
        if (childCount > 1) {
            tablePriceRange.removeViews(1, childCount - 1);
        }

        if (product == null || product.getWholeSellQty() == null || product.getWholeSellDiscount() == null) {
            return;
        }

        String[] qtyArray = product.getWholeSellQty().split(",");
        String[] priceArray = product.getWholeSellDiscount().split(",");

        // Use the smaller length to avoid IndexOutOfBoundsException
        int length = Math.min(qtyArray.length, priceArray.length);

        for (int i = 0; i < length; i++) {
            // Skip empty items resulting from trailing commas or empty strings
            String q = qtyArray[i].trim();
            String p = priceArray[i].trim();

            if (q.isEmpty() || p.isEmpty()) continue;

            TableRow row = new TableRow(this);
            row.setPadding(8, 8, 8, 8);

            TextView qtyTv = new TextView(this);
            qtyTv.setText(q);
            qtyTv.setPadding(16, 16, 16, 16);
            qtyTv.setTextColor(ContextCompat.getColor(this, R.color.black));
            qtyTv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            
            // Set layout params for even spacing
            TableRow.LayoutParams params = new TableRow.LayoutParams(0, TableRow.LayoutParams.WRAP_CONTENT, 1f);
            qtyTv.setLayoutParams(params);

            TextView priceTv = new TextView(this);
            priceTv.setText("₹ " + p);
            priceTv.setPadding(16, 16, 16, 16);
            priceTv.setTextColor(ContextCompat.getColor(this, R.color.black));
            priceTv.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            
             // Set layout params for even spacing
            priceTv.setLayoutParams(params);

            row.addView(qtyTv);
            row.addView(priceTv);

            tablePriceRange.addView(row);
        }
    }

}