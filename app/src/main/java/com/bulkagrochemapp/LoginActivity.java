package com.bulkagrochemapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
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

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilMobile, tilOtp;
    private EditText etMobile, etOtp;
    private Button btnSendOtp, btnVerifyOtp;
    private TextView txtSwitch, tvSubtitle, tvResendOtp;
    private String serverOtp;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

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
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnSendOtp.setOnClickListener(v -> {
            String phoneNumber = etMobile.getText().toString().trim();
            if (validatePhoneNumber(phoneNumber)) {
                checkUserAndSendOtp(phoneNumber);
            }
        });

        btnVerifyOtp.setOnClickListener(v1 -> {
            verifyOtp();
        });

        tvResendOtp.setOnClickListener(v -> {
            String phoneNumber = etMobile.getText().toString().trim();
            if (validatePhoneNumber(phoneNumber)) {
                checkUserAndSendOtp(phoneNumber);
            }
        });
    }

    private void initViews() {
        tilMobile = findViewById(R.id.tilMobile);
        tilOtp = findViewById(R.id.tilOtp);
        etMobile = findViewById(R.id.etMobile);
        etOtp = findViewById(R.id.etOtp);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        txtSwitch = findViewById(R.id.txtSwitch);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvResendOtp = findViewById(R.id.tvResendOtp);
    }

    private boolean validatePhoneNumber(String phone) {
        if (phone.length() != 10) {
            tilMobile.setError("Mobile number must be exactly 10 digits");
            return false;
        }
        if (isFakeNumber(phone)) {
            tilMobile.setError("Enter a valid mobile number");
            return false;
        }
        tilMobile.setError(null);
        return true;
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

    private void checkUserAndSendOtp(String phone) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.login(phone).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonString);
                        String status = jsonObject.optString("status");
                        String message = jsonObject.optString("message");

                        if ("success".equalsIgnoreCase(status)) {
                            requestOtp(phone, jsonObject.optJSONObject("user"));
                        } else {
                            Toast.makeText(LoginActivity.this, "Number is not registered", Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestOtp(String phone, JSONObject userData) {
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
                            Toast.makeText(LoginActivity.this, "OTP sent successfully!", Toast.LENGTH_SHORT).show();
                            
                            tilOtp.setVisibility(View.VISIBLE);
                            btnVerifyOtp.setVisibility(View.VISIBLE);
                            btnSendOtp.setVisibility(View.GONE);
                            tvResendOtp.setVisibility(View.VISIBLE);
                            
                            btnVerifyOtp.setTag(userData);
                        } else {
                            Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Failed to send OTP", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void verifyOtp() {
        String enteredOtp = etOtp.getText().toString().trim();
        if (serverOtp != null && enteredOtp.equals(serverOtp)) {
            JSONObject userData = (JSONObject) btnVerifyOtp.getTag();
            saveUserSession(userData);
            startActivity(new Intent(LoginActivity.this, MainActivity.class));
            finish();
        } else {
            tilOtp.setError("Invalid OTP");
        }
    }

    private void saveUserSession(JSONObject userData) {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("is_logged_in", true);
        
        if (userData != null) {
            editor.putString("id", userData.optString("id"));
            editor.putString("name", userData.optString("name"));
            editor.putString("email", userData.optString("email"));
            editor.putString("phone", userData.optString("phone"));
            
            // Fix: Check for "null" string or empty values from server
            String country = userData.optString("country");
            String state = userData.optString("state");
            String city = userData.optString("city");
            String zip = userData.optString("zip");
            String address = userData.optString("address");

            editor.putString("country", (country == null || country.equals("null")) ? "" : country);
            editor.putString("state", (state == null || state.equals("null")) ? "" : state);
            editor.putString("city", (city == null || city.equals("null")) ? "" : city);
            editor.putString("zip", (zip == null || zip.equals("null")) ? "" : zip);
            editor.putString("address", (address == null || address.equals("null")) ? "" : address);
        }
        
        editor.apply();
    }
}
