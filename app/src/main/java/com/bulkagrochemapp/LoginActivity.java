package com.bulkagrochemapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.util.Random;

public class LoginActivity extends AppCompatActivity {

    private EditText etPhoneNumber;
    private Button btnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            int paddingTop = insets.getInsets(WindowInsetsCompat.Type.systemBars()).top;
            v.setPadding(v.getPaddingLeft(), paddingTop + 24, v.getPaddingRight(), v.getPaddingBottom());
            return insets;
        });

        etPhoneNumber = findViewById(R.id.etPhoneNumber);
        btnSubmit = findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(v -> {
            String phoneNumber = etPhoneNumber.getText().toString().trim();
            if (phoneNumber.length() == 10) {
                // Generate a random 6-digit OTP
                String otp = String.format("%06d", new Random().nextInt(999999));

                // Simulate sending the OTP to the user's phone
                Toast.makeText(LoginActivity.this, "Your OTP is: " + otp, Toast.LENGTH_LONG).show();

                Intent intent = new Intent(LoginActivity.this, OtpActivity.class);
                intent.putExtra("otp", otp);
                intent.putExtra("phone", phoneNumber);
                startActivity(intent);
            } else {
                etPhoneNumber.setError("Please enter a valid 10-digit phone number");
            }
        });
    }
}
