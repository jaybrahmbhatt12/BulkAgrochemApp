package com.bulkagrochemapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.adapter.OrderAdapter;
import com.bulkagrochemapp.model.CartItem;
import com.bulkagrochemapp.model.Order;
import com.bulkagrochemapp.model.OrderResponse;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;
import com.bulkagrochemapp.utils.CartManager;
import com.bulkagrochemapp.utils.InvoiceGenerator;

import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrderHistoryActivity extends AppCompatActivity implements OrderAdapter.OnOrderActionListener {

    private RecyclerView rvOrders;
    private TextView txtNoOrders;
    private ImageButton btnBack;
    private OrderAdapter adapter;
    private List<Order> orderList = new ArrayList<>();
    private String userId;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_order_history);

        initViews();
        getUserId();
        fetchOrderHistory();

        btnBack.setOnClickListener(v -> finish());
    }

    private void initViews() {
        rvOrders = findViewById(R.id.rvOrders);
        txtNoOrders = findViewById(R.id.txtNoOrders);
        btnBack = findViewById(R.id.btnBack);

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrderAdapter(this, orderList, this);
        rvOrders.setAdapter(adapter);
    }

    private void getUserId() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        userId = prefs.getString("id", "");
    }

    private void fetchOrderHistory() {
        if (userId.isEmpty()) {
            Toast.makeText(this, "User not logged in", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getOrderHistory(userId).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    orderList.clear();
                    if (response.body().getData() != null) {
                        orderList.addAll(response.body().getData());
                    }
                    
                    if (orderList.isEmpty()) {
                        txtNoOrders.setVisibility(View.VISIBLE);
                        rvOrders.setVisibility(View.GONE);
                    } else {
                        txtNoOrders.setVisibility(View.GONE);
                        rvOrders.setVisibility(View.VISIBLE);
                        adapter.notifyDataSetChanged();
                    }
                } else {
                    txtNoOrders.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                txtNoOrders.setVisibility(View.VISIBLE);
                Toast.makeText(OrderHistoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReorder(Order order) {
        if (order.getItems() != null) {
            for (CartItem item : order.getItems()) {
                CartManager.addItem(item);
            }
            Toast.makeText(this, "Items added to cart", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, CartActivity.class));
        }
    }

    @Override
    public void onTrack(Order order) {
        Toast.makeText(this, "Tracking feature coming soon for Order #" + order.getId(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onCancel(Order order) {
        new AlertDialog.Builder(this)
                .setTitle("Cancel Order")
                .setMessage("Are you sure you want to cancel this order?")
                .setPositiveButton("Yes, Cancel", (dialog, which) -> performCancelOrder(order))
                .setNegativeButton("No", null)
                .show();
    }

    private void performCancelOrder(Order order) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.cancelOrder(order.getId(), userId).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(OrderHistoryActivity.this, "Order cancelled successfully", Toast.LENGTH_SHORT).show();
                    fetchOrderHistory(); // Refresh list
                } else {
                    Toast.makeText(OrderHistoryActivity.this, "Failed to cancel order", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(OrderHistoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onReturn(Order order) {
        final EditText etReason = new EditText(this);
        etReason.setHint("Enter reason for return");
        int padding = (int) (16 * getResources().getDisplayMetrics().density);
        
        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Return Order")
                .setView(etReason)
                .setPositiveButton("Submit Return", null)
                .setNegativeButton("Cancel", null)
                .create();

        dialog.setOnShowListener(dialogInterface -> {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(view -> {
                String reason = etReason.getText().toString().trim();
                if (reason.isEmpty()) {
                    etReason.setError("Reason is required");
                } else {
                    performReturnOrder(order, reason);
                    dialog.dismiss();
                }
            });
        });
        
        dialog.show();
        ((View) etReason.getParent()).setPadding(padding, padding, padding, padding);
    }

    private void performReturnOrder(Order order, String reason) {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.returnOrder(order.getId(), userId, reason).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(OrderHistoryActivity.this, "Return request submitted", Toast.LENGTH_SHORT).show();
                    fetchOrderHistory(); // Refresh list
                } else {
                    Toast.makeText(OrderHistoryActivity.this, "Failed to submit return request", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(OrderHistoryActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void onInvoice(Order order) {
        InvoiceGenerator.generateInvoice(this, order);
    }
}
