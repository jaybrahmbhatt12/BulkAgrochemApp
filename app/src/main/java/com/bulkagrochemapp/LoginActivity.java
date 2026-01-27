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
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class LoginActivity extends AppCompatActivity {

    private TextInputLayout tilMobile, tilOtp;
    private EditText etMobile, etOtp;
    private Button btnSendOtp, btnVerifyOtp;
    private TextView tvSubtitle, tvResendOtp;
    private String serverOtp;
    private boolean isNewUser = false;
    private JSONObject existingUserData;

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

        btnSendOtp.setOnClickListener(v -> {
            String phoneNumber = etMobile.getText().toString().trim();
            if (validatePhoneNumber(phoneNumber)) {
                checkUserAndSendOtp(phoneNumber);
            }
        });

        btnVerifyOtp.setOnClickListener(v -> {
            verifyOtp();
        });

        tvResendOtp.setOnClickListener(v -> {
            requestOtp(etMobile.getText().toString().trim());
        });
    }

    private void initViews() {
        tilMobile = findViewById(R.id.tilMobile);
        tilOtp = findViewById(R.id.tilOtp);
        etMobile = findViewById(R.id.etMobile);
        etOtp = findViewById(R.id.etOtp);
        btnSendOtp = findViewById(R.id.btnSendOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        tvSubtitle = findViewById(R.id.tvSubtitle);
        tvResendOtp = findViewById(R.id.tvResendOtp);
        
        // Hide unused fields for this flow
        if (findViewById(R.id.tilName) != null) findViewById(R.id.tilName).setVisibility(View.GONE);
        if (findViewById(R.id.tilEmail) != null) findViewById(R.id.tilEmail).setVisibility(View.GONE);
        TextView txtSwitch = findViewById(R.id.txtSwitch);
        if (txtSwitch != null) txtSwitch.setVisibility(View.GONE);
    }

    private boolean validatePhoneNumber(String phone) {
        if (phone.length() != 10) {
            tilMobile.setError("Enter 10-digit mobile number");
            return false;
        }
        tilMobile.setError(null);
        return true;
    }

    private void checkUserAndSendOtp(String phone) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.login(phone).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    String jsonString = response.isSuccessful() && response.body() != null ? response.body().string() : "";
                    if (!jsonString.isEmpty()) {
                        JSONObject jsonObject = new JSONObject(jsonString);
                        if ("success".equalsIgnoreCase(jsonObject.optString("status"))) {
                            isNewUser = false;
                            existingUserData = jsonObject.optJSONObject("user");
                        } else {
                            isNewUser = true;
                        }
                    } else {
                        isNewUser = true;
                    }
                    requestOtp(phone);
                } catch (Exception e) {
                    isNewUser = true;
                    requestOtp(phone);
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void requestOtp(String phone) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.sendOtp(phone).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        if ("success".equalsIgnoreCase(jsonObject.optString("status"))) {
                            serverOtp = jsonObject.optString("otp");
                            Toast.makeText(LoginActivity.this, "OTP sent successfully", Toast.LENGTH_SHORT).show();
                            
                            tilOtp.setVisibility(View.VISIBLE);
                            btnVerifyOtp.setVisibility(View.VISIBLE);
                            btnSendOtp.setVisibility(View.GONE);
                            tvResendOtp.setVisibility(View.VISIBLE);
                            tilMobile.setEnabled(false);
                            tvSubtitle.setText("Enter the OTP sent to +91 " + phone);
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
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
            if (isNewUser) {
                registerNewUserAndRedirect(etMobile.getText().toString().trim());
            } else {
                saveUserSession(existingUserData);
                startActivity(new Intent(LoginActivity.this, MainActivity.class));
                finish();
            }
        } else {
            tilOtp.setError("Invalid OTP");
        }
    }

    private void registerNewUserAndRedirect(String phone) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        // Use placeholder name/email for registration. User will update it in EditProfile.
        apiService.register("New User", "user@example.com", phone).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    fetchUserDataAndRedirect(phone);
                } else {
                    Toast.makeText(LoginActivity.this, "Registration failed", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(LoginActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void fetchUserDataAndRedirect(String phone) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.login(phone).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                try {
                    if (response.isSuccessful() && response.body() != null) {
                        JSONObject jsonObject = new JSONObject(response.body().string());
                        if ("success".equalsIgnoreCase(jsonObject.optString("status"))) {
                            saveUserSession(jsonObject.optJSONObject("user"));
                            // Redirect to Edit Profile for new user
                            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                            intent.putExtra("is_first_time", true);
                            startActivity(intent);
                            finish();
                        }
                    }
                } catch (Exception e) { e.printStackTrace(); }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {}
        });
    }

    private String sanitize(String input) {
        if (input == null || input.trim().equalsIgnoreCase("null") || input.isEmpty()) {
            return "";
        }
        return input.trim();
    }

    private void saveUserSession(JSONObject userData) {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("is_logged_in", true);
        if (userData != null) {
            editor.putString("id", sanitize(userData.optString("id")));
            editor.putString("name", sanitize(userData.optString("name")));
            editor.putString("email", sanitize(userData.optString("email")));
            editor.putString("phone", sanitize(userData.optString("phone")));
            editor.putString("country", sanitize(userData.optString("country", "")));
            editor.putString("state", sanitize(userData.optString("state", "")));
            editor.putString("city", sanitize(userData.optString("city", "")));
            editor.putString("zip", sanitize(userData.optString("zip", "")));
            editor.putString("address", sanitize(userData.optString("address", "")));
        }
        editor.apply();
    }
}
