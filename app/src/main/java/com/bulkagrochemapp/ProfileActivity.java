package com.bulkagrochemapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

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

        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String phoneNumber = prefs.getString("phone", "");
        String userName = prefs.getString("name", "User Name");

        if (!phoneNumber.isEmpty()) {
            tvLoggedInNumber.setText("+91 " + phoneNumber);
        }
        
        tvUserName.setText(userName);

        btnMyOrders.setOnClickListener(v -> {
            Intent intent = new Intent(ProfileActivity.this, OrderHistoryActivity.class);
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

        btnRefundPolicy.setOnClickListener(v -> openPage("refund", "Refund and Returns Policy"));
        btnShippingPolicy.setOnClickListener(v -> openPage("shipping-policy", "Shipping Policy"));

        btnLogout.setOnClickListener(v -> {
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    private void openPage(String slug, String title) {
        Intent intent = new Intent(this, PageDetailActivity.class);
        intent.putExtra("slug", slug);
        intent.putExtra("title", title);
        startActivity(intent);
    }

    @Override
    protected void onResume() {
        super.onResume();
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        tvUserName.setText(prefs.getString("name", "User Name"));
        String phoneNumber = prefs.getString("phone", "");
        if (!phoneNumber.isEmpty()) {
            tvLoggedInNumber.setText("+91 " + phoneNumber);
        }
    }
}
