package com.bulkagrochemapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputLayout;

import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilMobile, tilOtp;
    private EditText etMobile, etOtp;
    private Button btnSendOtp, btnVerifyOtp;
    private TextView txtSwitch, tvSubtitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                int paddingTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                v.setPadding(v.getPaddingLeft(), paddingTop, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });
        }

        // TextInputLayouts
        tilMobile = findViewById(R.id.tilMobile);
        tilOtp = findViewById(R.id.tilOtp);

        // EditTexts
        etMobile = findViewById(R.id.etMobile);
        etOtp = findViewById(R.id.etOtp);

        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        txtSwitch = findViewById(R.id.txtSwitch);
        tvSubtitle = findViewById(R.id.tvSubtitle);

        txtSwitch.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnSendOtp.setOnClickListener(v -> {
            String phoneNumber = etMobile.getText().toString().trim();
            if (phoneNumber.length() == 10) {
                // Generate a random 6-digit OTP
                String otp = String.format("%06d", new Random().nextInt(999999));

                // Simulate sending the OTP to the user's phone
                Toast.makeText(LoginActivity.this, "Your OTP is: " + otp, Toast.LENGTH_LONG).show();

                // Show OTP fields
                if (tilOtp != null) tilOtp.setVisibility(View.VISIBLE);
                if (btnVerifyOtp != null) btnVerifyOtp.setVisibility(View.VISIBLE);
                btnSendOtp.setVisibility(View.GONE);

                btnVerifyOtp.setOnClickListener(v1 -> {
                    String enteredOtp = etOtp.getText().toString().trim();
                    if (enteredOtp.equals(otp)) {
                        // Save login session
                        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
                        SharedPreferences.Editor editor = prefs.edit();
                        editor.putBoolean("is_logged_in", true);
                        editor.putString("phone", phoneNumber);
                        // In a real app, we would fetch the name from a database/server here.
                        // For now, we'll leave it as "User Name" if not registered.
                        editor.apply();

                        startActivity(new Intent(LoginActivity.this, MainActivity.class));
                        finish();
                    } else {
                        if (tilOtp != null) tilOtp.setError("Invalid OTP");
                    }
                });
            } else {
                if (tilMobile != null) tilMobile.setError("Please enter a valid 10-digit phone number");
            }
        });
    }
}
