package com.bulkagrochemapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

public class ProfileActivity extends BaseActivity {

    private TextView tvLoggedInNumber;
    private Button btnLogout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        tvLoggedInNumber = findViewById(R.id.tvLoggedInNumber);
        btnLogout = findViewById(R.id.btnLogout);

        SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
        String phoneNumber = prefs.getString("phone", "");

        if (!phoneNumber.isEmpty()) {
            tvLoggedInNumber.setText("+91 " + phoneNumber);
        }

        btnLogout.setOnClickListener(v -> {
            // Clear login state
            SharedPreferences.Editor editor = prefs.edit();
            editor.clear();
            editor.apply();

            // Redirect to LoginActivity
            Intent intent = new Intent(ProfileActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }
}
