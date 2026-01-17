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

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilMobile, tilOtp;
    private EditText etName, etEmail, etMobile, etOtp;
    private Button btnSendOtp, btnVerifyOtp;
    private TextView txtSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                int paddingTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
                v.setPadding(v.getPaddingLeft(), paddingTop, v.getPaddingRight(), v.getPaddingBottom());
                return insets;
            });
        }

        // TextInputLayouts
        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilMobile = findViewById(R.id.tilMobile);
        tilOtp = findViewById(R.id.tilOtp);

        // EditTexts
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etOtp = findViewById(R.id.etOtp);

        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        txtSwitch = findViewById(R.id.txtSwitch);

        txtSwitch.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnSendOtp.setOnClickListener(v -> {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phoneNumber = etMobile.getText().toString().trim();

            if (name.isEmpty()) {
                tilName.setError("Name is required");
                return;
            } else {
                tilName.setError(null);
            }
            if (email.isEmpty()) {
                tilEmail.setError("Email is required");
                return;
            } else {
                tilEmail.setError(null);
            }
            if (phoneNumber.length() == 10) {
                tilMobile.setError(null);
                // Generate a random 6-digit OTP
                String otp = String.format("%06d", new Random().nextInt(999999));

                // Simulate sending the OTP
                Toast.makeText(RegisterActivity.this, "Your OTP is: " + otp, Toast.LENGTH_LONG).show();

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
                        editor.putString("name", name);
                        editor.putString("email", email);
                        editor.putString("phone", phoneNumber);
                        editor.apply();

                        Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
                        startActivity(new Intent(RegisterActivity.this, MainActivity.class));
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
