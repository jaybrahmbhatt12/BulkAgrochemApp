package com.bulkagrochemapp;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bulkagrochemapp.model.CartItem;
import com.bulkagrochemapp.model.Product;
import com.bulkagrochemapp.model.Product;
import com.bulkagrochemapp.model.ProductResponse;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;
import com.bulkagrochemapp.utils.CartManager;
import com.bulkagrochemapp.utils.HtmlUtil;
import com.squareup.picasso.Picasso;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductDetailActivity extends AppCompatActivity {

    ImageView imgProduct;
    TextView txtName, txtPrice, txtStock, txtQty, txtDetails, txtDescription;
    TextView btnPlus, btnMinus;
    Button btnAddToCart;

    int quantity = 0;
    int initialQuantity=0,minimum_qty_value=0;
    private Product product;

    int productId;
    String name, image, stock, details, description , stocktype;
    double price;

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


        if (product != null) {
            // 🔹 Set data
            // for displaying data in product detail activity came trough search bar
            txtName.setText(product.getName());
            txtPrice.setText("₹" + product.getPrice() + "/"+product.getStocktype());
            txtStock.setText("In Stock"); // Assuming all products are in stock
            quantity = product.getMinimumQty();
            initialQuantity = quantity;
            txtQty.setText(String.valueOf(quantity));

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
        }
        else {
            // for displaying data in product detail activity came trough category activity
            txtName.setText(name);
            txtPrice.setText("₹" + price + "/" + stocktype);
            txtStock.setText(stock != null ? stock : "In Stock");
            quantity = minimum_qty_value;
            initialQuantity = quantity;
            txtQty.setText(String.valueOf(quantity));

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
        fetchProductDetails(productId);

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

    private void fetchProductDetails(int id) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        Call<ProductResponse> call = apiService.getProductDetails(id);

        call.enqueue(new Callback<ProductResponse>() {
            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().getData() != null && !response.body().getData().isEmpty()) {
                    Product product = response.body().getData().get(0);

                    // Update UI with fresh data
                    // Especially description and details which might be missing from the list item
                    if (product.getDescription() != null && !product.getDescription().isEmpty()) {
                        txtDescription.setText(HtmlUtil.fromHtml(product.getDescription()));
                        txtDescription.setVisibility(View.VISIBLE);
                    }

                    if (product.getDetails() != null && !product.getDetails().isEmpty()) {
                        txtDetails.setText(HtmlUtil.fromHtml(product.getDetails()));
                        txtDetails.setVisibility(View.VISIBLE);
                    }
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                // Ignore error, stick with Intent data
            }
        });
    }
}
