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
import com.bulkagrochemapp.utils.CartManager;
import com.bulkagrochemapp.utils.HtmlUtil;
import com.squareup.picasso.Picasso;

public class ProductDetailActivity extends AppCompatActivity {

    ImageView imgProduct;
    TextView txtName, txtPrice, txtStock, txtQty, txtDetails;
    TextView btnPlus, btnMinus;
    Button btnAddToCart;

    int quantity = 5;
    private Product product;

    int productId;
    String name, image, stock, details;
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
        stock = getIntent().getStringExtra("stock");
        details = getIntent().getStringExtra("product_details");

        if (product != null) {
            // 🔹 Set data
            // for displaying data in product detail activity came trough search bar
            txtName.setText(product.getName());
            txtPrice.setText("₹" + product.getPrice());
            txtStock.setText("In Stock"); // Assuming all products are in stock
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
            txtPrice.setText("₹" + price);
            txtStock.setText(stock != null ? stock : "In Stock");
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


        // 🔹 Quantity +
        btnPlus.setOnClickListener(v -> {
            quantity+=5;
            txtQty.setText(String.valueOf(quantity));
        });

        // 🔹 Quantity -
        btnMinus.setOnClickListener(v -> {
            if (quantity > 5) {
                quantity-=5;
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
}
