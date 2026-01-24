package com.bulkagrochemapp;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class PaymentFailureActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_payment_failure);

        Button btnRepayment = findViewById(R.id.btnRepayment);
        Button btnHomePage = findViewById(R.id.btnHomePage);

        btnRepayment.setOnClickListener(v -> {
            // Simply finish this activity to go back to OrderSummaryActivity
            // where the user can click "Pay Now" again.
            finish();
        });

        btnHomePage.setOnClickListener(v -> {
            Intent intent = new Intent(PaymentFailureActivity.this, MainActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        // Default back behavior is fine, it will return to OrderSummaryActivity
    }
}
