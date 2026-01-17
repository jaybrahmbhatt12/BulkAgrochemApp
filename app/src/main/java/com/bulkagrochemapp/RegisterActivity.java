package com.bulkagrochemapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Patterns;
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
    private TextView txtSwitch, tvResendOtp;
    private String generatedOtp;

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

        initViews();

        txtSwitch.setOnClickListener(v -> {
            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
            startActivity(intent);
            finish();
        });

        btnSendOtp.setOnClickListener(v -> {
            if (validateInputs()) {
                sendOtp();
            }
        });

        btnVerifyOtp.setOnClickListener(v -> {
            verifyOtpAndRegister();
        });

        tvResendOtp.setOnClickListener(v -> {
            if (validateInputs()) {
                sendOtp();
                Toast.makeText(this, "OTP Resent Successfully!", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void initViews() {
        tilName = findViewById(R.id.tilName);
        tilEmail = findViewById(R.id.tilEmail);
        tilMobile = findViewById(R.id.tilMobile);
        tilOtp = findViewById(R.id.tilOtp);
        etName = findViewById(R.id.etName);
        etEmail = findViewById(R.id.etEmail);
        etMobile = findViewById(R.id.etMobile);
        etOtp = findViewById(R.id.etOtp);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        txtSwitch = findViewById(R.id.txtSwitch);
        tvResendOtp = findViewById(R.id.tvResendOtp);
    }

    private boolean validateInputs() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String mobile = etMobile.getText().toString().trim();

        boolean isValid = true;

        if (name.isEmpty()) {
            tilName.setError("Full name is required");
            isValid = false;
        } else {
            tilName.setError(null);
        }

        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Enter a valid email address (e.g., user@example.com)");
            isValid = false;
        } else {
            tilEmail.setError(null);
        }

        if (mobile.length() != 10) {
            tilMobile.setError("Mobile number must be exactly 10 digits");
            isValid = false;
        } else if (isFakeNumber(mobile)) {
            tilMobile.setError("Enter a valid mobile number");
            isValid = false;
        } else {
            tilMobile.setError(null);
        }

        return isValid;
    }

    private boolean isFakeNumber(String mobile) {
        char firstChar = mobile.charAt(0);
        boolean allSame = true;
        for (int i = 1; i < mobile.length(); i++) {
            if (mobile.charAt(i) != firstChar) {
                allSame = false;
                break;
            }
        }
        return allSame;
    }

    private void sendOtp() {
        // Generate a random 6-digit OTP
        generatedOtp = String.format("%06d", new Random().nextInt(999999));

        // TODO: Integrate SMS API here to send generatedOtp to etMobile
        // For now, simulate sending the OTP
        Toast.makeText(this, "OTP sent to your mobile: " + generatedOtp, Toast.LENGTH_LONG).show();

        tilOtp.setVisibility(View.VISIBLE);
        btnVerifyOtp.setVisibility(View.VISIBLE);
        btnSendOtp.setVisibility(View.GONE);
        tvResendOtp.setVisibility(View.VISIBLE);
    }

    private void verifyOtpAndRegister() {
        String enteredOtp = etOtp.getText().toString().trim();
        if (enteredOtp.equals(generatedOtp)) {
            String name = etName.getText().toString().trim();
            String email = etEmail.getText().toString().trim();
            String phone = etMobile.getText().toString().trim();
            
            saveUserSession(name, email, phone);
            
            Toast.makeText(this, "Registration Successful!", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(RegisterActivity.this, MainActivity.class));
            finish();
        } else {
            tilOtp.setError("Invalid OTP");
        }
    }

    private void saveUserSession(String name, String email, String phone) {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("is_logged_in", true);
        editor.putString("name", name);
        editor.putString("email", email);
        editor.putString("phone", phone);
        editor.apply();
    }
}
