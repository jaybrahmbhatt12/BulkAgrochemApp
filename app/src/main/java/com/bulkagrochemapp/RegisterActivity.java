package com.bulkagrochemapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.Random;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RegisterActivity extends AppCompatActivity {

    private TextInputLayout tilName, tilEmail, tilMobile, tilOtp;
    private EditText etName, etEmail, etMobile, etOtp;
    private Button btnSendOtp, btnVerifyOtp;
    private TextView txtSwitch, tvResendOtp;
    private String serverOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        View mainView = findViewById(R.id.main);
        if (mainView != null) {
            ViewCompat.setOnApplyWindowInsetsListener(mainView, (v, insets) -> {
                Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
                Insets ime = insets.getInsets(WindowInsetsCompat.Type.ime());
                
                int paddingBottom = Math.max(systemBars.bottom, ime.bottom);
                v.setPadding(v.getPaddingLeft(), systemBars.top, v.getPaddingRight(), paddingBottom);
                
                return WindowInsetsCompat.CONSUMED;
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
                requestOtp();
            }
        });

        btnVerifyOtp.setOnClickListener(v -> {
            verifyOtpAndRegister();
        });

        tvResendOtp.setOnClickListener(v -> {
            if (validateInputs()) {
                requestOtp();
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

    private void requestOtp() {
        String phone = etMobile.getText().toString().trim();
        
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.sendOtp(phone).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonString);
                        String status = jsonObject.optString("status");
                        String message = jsonObject.optString("message");

                        if ("success".equalsIgnoreCase(status)) {
                            serverOtp = jsonObject.optString("otp");
                            Toast.makeText(RegisterActivity.this, "OTP sent successfully to " + phone, Toast.LENGTH_SHORT).show();
                            
                            tilOtp.setVisibility(View.VISIBLE);
                            btnVerifyOtp.setVisibility(View.VISIBLE);
                            btnSendOtp.setVisibility(View.GONE);
                            tvResendOtp.setVisibility(View.VISIBLE);
                        } else {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyOtpAndRegister() {
        String enteredOtp = etOtp.getText().toString().trim();
        if (serverOtp != null && enteredOtp.equals(serverOtp)) {
            performRegistration();
        } else {
            tilOtp.setError("Invalid OTP");
        }
    }

    private void performRegistration() {
        String name = etName.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String phone = etMobile.getText().toString().trim();

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.register(name, email, phone).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonString);
                        String status = jsonObject.optString("status");
                        String message = jsonObject.optString("message");

                        if ("success".equalsIgnoreCase(status)) {
                            Toast.makeText(RegisterActivity.this, "Registration Successful! Please login.", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            startActivity(intent);
                            finish();
                        } else {
                            Toast.makeText(RegisterActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(RegisterActivity.this, "Error parsing response", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(RegisterActivity.this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(RegisterActivity.this, "Network Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
