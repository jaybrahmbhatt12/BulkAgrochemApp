package com.bulkagrochemapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bulkagrochemapp.model.CartItem;
import com.bulkagrochemapp.utils.CartManager;
import com.squareup.picasso.Picasso;

public class ProductDetailActivity extends AppCompatActivity {

    ImageView imgProduct;
    TextView txtName, txtPrice, txtStock, txtQty;
    TextView btnPlus, btnMinus;
    Button btnAddToCart;

    int quantity = 5;

    int productId;
    String name, image, stock;
    double price;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // 🔹 Bind views
        imgProduct = findViewById(R.id.imgProduct);
        txtName = findViewById(R.id.txtProductName);
        txtPrice = findViewById(R.id.txtProductPrice);
        txtStock = findViewById(R.id.txtStock);
        txtQty = findViewById(R.id.txtQty);
        btnPlus = findViewById(R.id.btnPlus);
        btnMinus = findViewById(R.id.btnMinus);
        btnAddToCart = findViewById(R.id.btnAddToCart);

        // 🔹 Get intent data
        productId = getIntent().getIntExtra("product_id", 0);
        name = getIntent().getStringExtra("product_name");
        image = getIntent().getStringExtra("product_image");
        price = getIntent().getDoubleExtra("product_price", 0);
        stock = getIntent().getStringExtra("stock");

        // 🔹 Set data
        txtName.setText(name);
        txtPrice.setText("₹" + price);
        txtStock.setText(stock != null ? stock : "In Stock");
        txtQty.setText(String.valueOf(quantity));

        Picasso.get()
                .load(image)
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(imgProduct);

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

            CartItem cartItem =
                    new CartItem(productId, name, image, price);

            for (int i = 1; i < quantity; i++) {
                cartItem.increaseQty();
            }

            CartManager.addToCart(cartItem);

            Toast.makeText(
                    ProductDetailActivity.this,
                    "Added to cart",
                    Toast.LENGTH_SHORT
            ).show();
        });
    }
}
