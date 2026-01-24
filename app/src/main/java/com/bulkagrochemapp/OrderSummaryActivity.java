package com.bulkagrochemapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.adapter.CouponAdapter;
import com.bulkagrochemapp.adapter.OrderSummaryAdapter;
import com.bulkagrochemapp.model.CartItem;
import com.bulkagrochemapp.model.Coupon;
import com.bulkagrochemapp.model.CouponResponse;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;
import com.bulkagrochemapp.utils.CartManager;
import com.google.gson.Gson;
import com.razorpay.Checkout;
import com.razorpay.PaymentResultListener;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import okhttp3.MediaType;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderSummaryActivity extends AppCompatActivity implements PaymentResultListener {

    private RecyclerView rvOrderItems, rvCoupons;
    private EditText etCoupon;
    private Button btnApplyCoupon, btnPayNow;
    private ImageButton btnBack;
    private TextView btnChangeAddress;
    private TextView txtAddressName, txtAddressDetails, txtAddressPhone;
    private TextView txtPriceItems, txtDiscountAmount, txtTax, txtTotalAmount, txtBottomTotal, txtCouponStatus;
    private LinearLayout layoutDiscount;

    private double subtotal = 0;
    private double tax = 0;
    private double discount = 0;
    private double grandTotal = 0;
    private List<Coupon> availableCoupons;
    private String appliedCouponCode = "";

    private final ActivityResultLauncher<Intent> changeAddressLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK) {
                    loadAddress();
                }
            }
    );

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_summary);

        Checkout.preload(getApplicationContext());

        initViews();
        loadAddress();
        setupOrderList();
        calculateTotals();
        loadCoupons();

        btnBack.setOnClickListener(v -> finish());
        
        btnApplyCoupon.setOnClickListener(v -> applyCoupon());

        btnPayNow.setOnClickListener(v -> {
            if (validateDetails()) {
                startPayment();
            }
        });

        btnChangeAddress.setOnClickListener(v -> {
            Intent intent = new Intent(OrderSummaryActivity.this, ChangeAddressActivity.class);
            changeAddressLauncher.launch(intent);
        });
    }

    private void initViews() {
        rvOrderItems = findViewById(R.id.rvOrderItems);
        rvCoupons = findViewById(R.id.rvCoupons);
        etCoupon = findViewById(R.id.etCoupon);
        btnApplyCoupon = findViewById(R.id.btnApplyCoupon);
        btnPayNow = findViewById(R.id.btnPayNow);
        btnBack = findViewById(R.id.btnBack);
        btnChangeAddress = findViewById(R.id.btnChangeAddress);
        
        txtAddressName = findViewById(R.id.txtAddressName);
        txtAddressDetails = findViewById(R.id.txtAddressDetails);
        txtAddressPhone = findViewById(R.id.txtAddressPhone);

        txtPriceItems = findViewById(R.id.txtPriceItems);
        txtDiscountAmount = findViewById(R.id.txtDiscountAmount);
        txtTax = findViewById(R.id.txtTax);
        txtTotalAmount = findViewById(R.id.txtTotalAmount);
        txtBottomTotal = findViewById(R.id.txtBottomTotal);
        txtCouponStatus = findViewById(R.id.txtCouponStatus);
        layoutDiscount = findViewById(R.id.layoutDiscount);

        rvCoupons.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
    }

    private void loadAddress() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String name = prefs.getString("name", "User Name");
        String phone = prefs.getString("phone", "Phone Number");
        String address = prefs.getString("address", "");
        String city = prefs.getString("district", "");
        String state = prefs.getString("state", "");
        String zip = prefs.getString("zip", "");

        txtAddressName.setText(name);
        txtAddressPhone.setText(phone);
        
        StringBuilder fullAddress = new StringBuilder();
        if (!address.isEmpty()) fullAddress.append(address);
        if (!city.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(city);
        }
        if (!state.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(", ");
            fullAddress.append(state);
        }
        if (!zip.isEmpty()) {
            if (fullAddress.length() > 0) fullAddress.append(" ");
            fullAddress.append(zip);
        }

        if (fullAddress.length() > 0) {
            txtAddressDetails.setText(fullAddress.toString());
        } else {
            txtAddressDetails.setText("Please update your address in profile");
        }
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
                    if (availableCoupons != null) {
                        CouponAdapter adapter = new CouponAdapter(OrderSummaryActivity.this, availableCoupons);
                        rvCoupons.setAdapter(adapter);
                    }
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
            if (subtotal < selectedCoupon.getAbovePrice()) {
                txtCouponStatus.setVisibility(View.VISIBLE);
                txtCouponStatus.setText("Minimum order of ₹" + String.format("%.0f", selectedCoupon.getAbovePrice()) + " required for this coupon");
                txtCouponStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
                discount = 0;
                appliedCouponCode = "";
            } else {
                if (selectedCoupon.getType() == 1) { 
                    discount = selectedCoupon.getPrice();
                } else { 
                    discount = (subtotal * selectedCoupon.getPrice()) / 100;
                }
                
                appliedCouponCode = selectedCoupon.getCode();
                txtCouponStatus.setVisibility(View.VISIBLE);
                txtCouponStatus.setText(String.format(Locale.getDefault(), "Coupon applied! You saved ₹%.2f", discount));
                txtCouponStatus.setTextColor(getResources().getColor(R.color.green));
            }
        } else {
            txtCouponStatus.setVisibility(View.VISIBLE);
            txtCouponStatus.setText("Invalid or expired coupon code");
            txtCouponStatus.setTextColor(getResources().getColor(android.R.color.holo_red_dark));
            discount = 0;
            appliedCouponCode = "";
        }

        updateUI();
    }

    private boolean validateDetails() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String uid = prefs.getString("id", "");
        String name = prefs.getString("name", "");
        String email = prefs.getString("email", "");
        String phone = prefs.getString("phone", "");
        String address = prefs.getString("address", "");
        String city = prefs.getString("district", "");
        String state = prefs.getString("state", "");
        String zip = prefs.getString("zip", "");

        if (uid.isEmpty()) {
            Toast.makeText(this, "Please login to place order", Toast.LENGTH_SHORT).show();
            return false;
        }

        if (address.isEmpty() || city.isEmpty() || state.isEmpty() || email.isEmpty() || name.isEmpty() || phone.isEmpty()) {
            Toast.makeText(this, "Please update your full profile (Email, Address, City, State) to place an order", Toast.LENGTH_LONG).show();
            return false;
        }

        if (CartManager.getCartItems().isEmpty()) {
            Toast.makeText(this, "Your cart is empty", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }

    private void startPayment() {
        Checkout checkout = new Checkout();
        checkout.setKeyID("rzp_test_RZEWfckatMa4lh");

        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String name = prefs.getString("name", "User");
        String email = prefs.getString("email", "");
        String phone = prefs.getString("phone", "");

        try {
            JSONObject options = new JSONObject();
            options.put("name", "BulkAgrochem");
            options.put("description", "Order Payment");
            options.put("image", "https://bulkapi.tiffinworld.com/assets/logo_bulkagro.png");
            options.put("currency", "INR");
            options.put("amount", Math.round(grandTotal * 100)); // Amount in paisa

            JSONObject prefill = new JSONObject();
            prefill.put("email", email);
            prefill.put("contact", phone);
            options.put("prefill", prefill);

            checkout.open(this, options);
        } catch (Exception e) {
            Toast.makeText(this, "Error in starting Razorpay: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onPaymentSuccess(String razorpayPaymentID) {
        placeOrder(razorpayPaymentID);
    }

    @Override
    public void onPaymentError(int code, String response) {
        // Show Payment Failure Page
        Intent intent = new Intent(OrderSummaryActivity.this, PaymentFailureActivity.class);
        startActivity(intent);
    }

    private void placeOrder(String paymentId) {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String uid = prefs.getString("id", "");
        String name = prefs.getString("name", "");
        String email = prefs.getString("email", "");
        String phone = prefs.getString("phone", "");
        String address = prefs.getString("address", "");
        String city = prefs.getString("district", "");
        String state = prefs.getString("state", "");
        String zip = prefs.getString("zip", "");

        List<CartItem> cartItems = CartManager.getCartItems();
        int totalQty = 0;
        for (CartItem item : cartItems) {
            totalQty += item.getQuantity();
        }

        String cartJson = new Gson().toJson(cartItems);

        btnPayNow.setEnabled(false);
        btnPayNow.setText("Placing Order...");

        JSONObject orderJson = new JSONObject();
        try {
            orderJson.put("user_id", uid);
            orderJson.put("cart", cartJson);
            orderJson.put("method", "Razorpay");
            orderJson.put("payment_id", paymentId);
            orderJson.put("shipping", "shipto");
            orderJson.put("totalQty", totalQty);
            orderJson.put("pay_amount", grandTotal);
            orderJson.put("totalprice", subtotal);
            orderJson.put("tax", tax);

            orderJson.put("customer_name", name);
            orderJson.put("customer_email", email);
            orderJson.put("customer_phone", phone);
            orderJson.put("customer_address", address);
            orderJson.put("customer_city", city);
            orderJson.put("customer_state", state);
            orderJson.put("customer_zip", zip);
            orderJson.put("customer_country", "India");

            orderJson.put("coupon_code", appliedCouponCode);
            orderJson.put("coupon_discount", discount);

        } catch (JSONException e) {
            e.printStackTrace();
        }

        RequestBody body = RequestBody.create(
                MediaType.parse("application/json"),
                orderJson.toString()
        );

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.placeOrder(body).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String result = response.body().string();
                        JSONObject jsonObject = new JSONObject(result);
                        String status = jsonObject.optString("status");

                        if ("success".equalsIgnoreCase(status)) {
                            // Clear cart locally and on server
                            CartManager.clearCart(OrderSummaryActivity.this);

                            Intent intent = new Intent(OrderSummaryActivity.this, PaymentSuccessActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            btnPayNow.setEnabled(true);
                            btnPayNow.setText("Pay Now");
                            Toast.makeText(OrderSummaryActivity.this, "Server Error: " + jsonObject.optString("message"), Toast.LENGTH_LONG).show();
                        }
                    } catch (IOException | JSONException e) {
                        btnPayNow.setEnabled(true);
                        btnPayNow.setText("Pay Now");
                        Toast.makeText(OrderSummaryActivity.this, "Response Parsing Error", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    btnPayNow.setEnabled(true);
                    btnPayNow.setText("Pay Now");
                    Toast.makeText(OrderSummaryActivity.this, "Failed to place order", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                btnPayNow.setEnabled(true);
                btnPayNow.setText("Pay Now");
                Toast.makeText(OrderSummaryActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
