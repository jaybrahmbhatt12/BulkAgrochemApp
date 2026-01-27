package com.bulkagrochemapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

public class ProfileActivity extends BaseActivity {

    private TextView tvLoggedInNumber, tvUserName;
    private View btnLogout, btnEditProfile, btnHelpSupport, btnRefundPolicy, btnShippingPolicy, btnMyOrders;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvLoggedInNumber = findViewById(R.id.tvLoggedInNumber);
        tvUserName = findViewById(R.id.tvUserName);
        btnLogout = findViewById(R.id.btnLogout);
        btnEditProfile = findViewById(R.id.btnEditProfile);
        btnHelpSupport = findViewById(R.id.btnHelpSupport);
        btnRefundPolicy = findViewById(R.id.btnRefundPolicy);
        btnShippingPolicy = findViewById(R.id.btnShippingPolicy);
        btnMyOrders = findViewById(R.id.btnMyOrders);

        updateProfileUI();

        btnMyOrders.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, MyOrdersActivity.class);
            startActivity(intent);
        });

        btnEditProfile.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, EditProfileActivity.class);
            startActivity(intent);
        });

        btnHelpSupport.setOnClickListener(v -> {
            String whatsappNumber = "+919512017161";
            String message = "Hello, I need help regarding BulkAgrochem app.";
            String url = "https://api.whatsapp.com/send?phone=" + whatsappNumber + "&text=" + Uri.encode(message);

            try {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            } catch (Exception e) {
                Toast.makeText(this, "WhatsApp is not installed on this device", Toast.LENGTH_SHORT).show();
            }
        });

        btnRefundPolicy.setOnClickListener(v -> openPage("refund_returns", "Refund and Returns Policy"));
        btnShippingPolicy.setOnClickListener(v -> openPage("shipping-policy", "Shipping Policy"));

        btnLogout.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Logout")
                    .setMessage("Are you sure you want to logout?")
                    .setPositiveButton("Logout", (dialog, which) -> {
                        SharedPreferences.Editor editor = getSharedPreferences("user_session", MODE_PRIVATE).edit();
                        editor.clear();
                        editor.apply();

                        Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                        startActivity(intent);
                        finish();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void openPage(String slug, String title) {
        Intent intent = new Intent(this, PageDetailActivity.class);
        intent.putExtra("slug", slug);
        intent.putExtra("title", title);
        startActivity(intent);
    }

    private String sanitize(String input) {
        if (input == null || input.trim().equalsIgnoreCase("null") || input.isEmpty()) {
            return "";
        }
        return input.trim();
    }

    private void updateProfileUI() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String name = sanitize(prefs.getString("name", ""));
        String phoneNumber = sanitize(prefs.getString("phone", ""));

        if (name.isEmpty()) name = "User Name";
        tvUserName.setText(name);

        if (!phoneNumber.isEmpty()) {
            tvLoggedInNumber.setText("+91 " + phoneNumber);
        } else {
            tvLoggedInNumber.setText("Phone Number");
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        updateProfileUI();
    }
}
