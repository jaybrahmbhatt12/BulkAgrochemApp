package com.bulkagrochemapp;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.adapter.OrderSummaryAdapter;
import com.bulkagrochemapp.model.CartItem;
import com.bulkagrochemapp.model.Coupon;
import com.bulkagrochemapp.model.CouponResponse;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;
import com.bulkagrochemapp.utils.CartManager;

import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderSummaryActivity extends AppCompatActivity {

    private RecyclerView rvOrderItems;
    private EditText etCoupon;
    private Button btnApplyCoupon, btnPayNow;
    private ImageButton btnBack;
    private TextView btnChangeAddress;
    private TextView txtPriceItems, txtDiscountAmount, txtTax, txtTotalAmount, txtBottomTotal, txtCouponStatus;
    private LinearLayout layoutDiscount;

    private double subtotal = 0;
    private double tax = 0;
    private double discount = 0;
    private double grandTotal = 0;
    private List<Coupon> availableCoupons;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);

        initViews();
        setupOrderList();
        calculateTotals();
        loadCoupons();

        btnBack.setOnClickListener(v -> finish());

        btnApplyCoupon.setOnClickListener(v -> applyCoupon());

        btnPayNow.setOnClickListener(v -> {
            Toast.makeText(this, "Order Placed Successfully!", Toast.LENGTH_LONG).show();
            CartManager.clearCart();
            finish();
        });

        btnChangeAddress.setOnClickListener(v -> {
            Toast.makeText(this, "Change Address feature coming soon", Toast.LENGTH_SHORT).show();
        });
    }

    private void initViews() {
        rvOrderItems = findViewById(R.id.rvOrderItems);
        etCoupon = findViewById(R.id.etCoupon);
        btnApplyCoupon = findViewById(R.id.btnApplyCoupon);
        btnPayNow = findViewById(R.id.btnPayNow);
        btnBack = findViewById(R.id.btnBack);
        btnChangeAddress = findViewById(R.id.btnChangeAddress);

        txtPriceItems = findViewById(R.id.txtPriceItems);
        txtDiscountAmount = findViewById(R.id.txtDiscountAmount);
        txtTax = findViewById(R.id.txtTax);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);
        txtBottomTotal = findViewById(R.id.txtBottomTotal);
        txtCouponStatus = findViewById(R.id.txtCouponStatus);
        layoutDiscount = findViewById(R.id.layoutDiscount);
    }

    private void setupOrderList() {
        rvOrderItems.setLayoutManager(new LinearLayoutManager(this));
        OrderSummaryAdapter adapter = new OrderSummaryAdapter(this, CartManager.getCartItems());
        rvOrderItems.setAdapter(adapter);
    }

    private void calculateTotals() {
        subtotal = CartManager.getSubtotalBeforeTax();
        tax = CartManager.getTotalTaxAmount();
        updateUI();
    }

    private void updateUI() {
        grandTotal = subtotal + tax - discount;
        if (grandTotal < 0) grandTotal = 0;

        txtPriceItems.setText(String.format(Locale.getDefault(), "₹%.2f", subtotal));
        txtTax.setText(String.format(Locale.getDefault(), "₹%.2f", tax));

        if (discount > 0) {
            layoutDiscount.setVisibility(View.VISIBLE);
            txtDiscountAmount.setText(String.format(Locale.getDefault(), "-₹%.2f", discount));
        } else {
            layoutDiscount.setVisibility(View.GONE);
        }

        txtTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", grandTotal));
        txtBottomTotal.setText(String.format(Locale.getDefault(), "₹%.2f", grandTotal));
    }

    private void loadCoupons() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getCoupons().enqueue(new Callback<CouponResponse>() {
            @Override
            public void onResponse(Call<CouponResponse> call, Response<CouponResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    availableCoupons = response.body().getData();
                }
            }

            @Override
            public void onFailure(Call<CouponResponse> call, Throwable t) {
                // Silently fail or log
            }
        });
    }

    private void applyCoupon() {
        String code = etCoupon.getText().toString().trim();
        if (code.isEmpty()) {
            Toast.makeText(this, "Please enter a coupon code", Toast.LENGTH_SHORT).show();
            return;
        }

        if (availableCoupons == null || availableCoupons.isEmpty()) {
            Toast.makeText(this, "No coupons available", Toast.LENGTH_SHORT).show();
            return;
        }

        Coupon selectedCoupon = null;
        for (Coupon coupon : availableCoupons) {
            if (coupon.getCode().equalsIgnoreCase(code)) {
                selectedCoupon = coupon;
                break;
            }
        }

        if (selectedCoupon != null) {
            // Check minimum purchase amount
            if (subtotal < selectedCoupon.getAbovePrice()) {
                txtCouponStatus.setVisibility(View.VISIBLE);
                txtCouponStatus.setText("Minimum purchase of ₹" + selectedCoupon.getAbovePrice() + " required");
                txtCouponStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                discount = 0;
            } else {
                // Apply discount logic
                if (selectedCoupon.getType() == 1) { // Percentage
                    discount = (subtotal * selectedCoupon.getPrice()) / 100;
                } else { // Fixed Amount
                    discount = selectedCoupon.getPrice();
                }

                txtCouponStatus.setVisibility(View.VISIBLE);
                txtCouponStatus.setText("Coupon applied! Discount: ₹" + String.format("%.2f", discount));
                txtCouponStatus.setTextColor(getResources().getColor(R.color.green));
            }
        } else {
            txtCouponStatus.setVisibility(View.VISIBLE);
            txtCouponStatus.setText("Invalid coupon code");
            txtCouponStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            discount = 0;
        }

        updateUI();
    }
}
