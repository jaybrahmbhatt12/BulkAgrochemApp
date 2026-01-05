package com.bulkagrochemapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class OtpActivity extends AppCompatActivity {

    private EditText etOtp;
    private Button btnVerifyOtp;
    private TextView tvOtpMessage;

    private String generatedOtp;
    private String phoneNumber;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_otp);

        etOtp = findViewById(R.id.etOtp);
        btnVerifyOtp = findViewById(R.id.btnVerifyOtp);
        tvOtpMessage = findViewById(R.id.tvOtpMessage);

        generatedOtp = getIntent().getStringExtra("otp");
        phoneNumber = getIntent().getStringExtra("phone");

        tvOtpMessage.setText("An OTP has been sent to +91" + phoneNumber);

        btnVerifyOtp.setOnClickListener(v -> {
            String enteredOtp = etOtp.getText().toString().trim();
            if (enteredOtp.equals(generatedOtp)) {
                // Save the login state
                SharedPreferences prefs = getSharedPreferences("login_prefs", MODE_PRIVATE);
                SharedPreferences.Editor editor = prefs.edit();
                editor.putString("phone", phoneNumber);
                editor.apply();

                Toast.makeText(OtpActivity.this, "OTP Verified!", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(OtpActivity.this, MainActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                finish(); // Prevent going back to the OTP or Login screen
            } else {
                etOtp.setError("Invalid OTP");
            }
        });
    }
}
