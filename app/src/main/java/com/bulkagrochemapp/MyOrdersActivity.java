package com.bulkagrochemapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.bulkagrochemapp.adapter.OrdersAdapter;
import com.bulkagrochemapp.model.Order;
import com.bulkagrochemapp.model.OrderResponse;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;
import com.google.android.material.chip.ChipGroup;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MyOrdersActivity extends BaseActivity implements OrdersAdapter.OnOrderActionListener {

    private RecyclerView rvOrders;
    private SwipeRefreshLayout swipeRefresh;
    private ImageButton btnBack;
    private ChipGroup chipGroupFilters;
    private OrdersAdapter adapter;
    private LinearLayout layoutEmpty;
    private TextView tvEmptyMessage;
    
    private List<Order> allOrdersList = new ArrayList<>();
    private List<List<Order>> groupedOrders = new ArrayList<>();
    private String currentFilter = "All";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_orders);

        rvOrders = findViewById(R.id.rvOrders);
        swipeRefresh = findViewById(R.id.swipeRefresh);
        btnBack = findViewById(R.id.btnBack);
        chipGroupFilters = findViewById(R.id.chipGroupFilters);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        tvEmptyMessage = findViewById(R.id.tvEmptyMessage);

        btnBack.setOnClickListener(v -> finish());

        rvOrders.setLayoutManager(new LinearLayoutManager(this));
        adapter = new OrdersAdapter(this, groupedOrders, this);
        rvOrders.setAdapter(adapter);

        swipeRefresh.setOnRefreshListener(this::fetchOrders);

        setupFilters();
        fetchOrders();
    }

    private void setupFilters() {
        chipGroupFilters.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.chipAll) {
                currentFilter = "All";
            } else if (checkedId == R.id.chipPending) {
                currentFilter = "pending";
            } else if (checkedId == R.id.chipProcessing) {
                currentFilter = "processing";
            } else if (checkedId == R.id.chipCompleted) {
                currentFilter = "completed";
            }
            applyFilterAndGroup();
        });
    }

    private void fetchOrders() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String userId = prefs.getString("id", null);

        if (userId == null) {
            Toast.makeText(this, "Please login to see your orders", Toast.LENGTH_SHORT).show();
            swipeRefresh.setRefreshing(false);
            return;
        }

        swipeRefresh.setRefreshing(true);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.fetchOrders(userId).enqueue(new Callback<OrderResponse>() {
            @Override
            public void onResponse(Call<OrderResponse> call, Response<OrderResponse> response) {
                swipeRefresh.setRefreshing(false);
                if (response.isSuccessful() && response.body() != null) {
                    if ("success".equals(response.body().getStatus())) {
                        allOrdersList = response.body().getData();
                        Collections.sort(allOrdersList, (o1, o2) -> o2.getCreatedAt().compareTo(o1.getCreatedAt()));
                        applyFilterAndGroup();
                    } else if ("empty".equals(response.body().getStatus())) {
                        allOrdersList.clear();
                        groupedOrders.clear();
                        adapter.notifyDataSetChanged();
                        updateEmptyState(true);
                    }
                } else {
                    Toast.makeText(MyOrdersActivity.this, "Failed to fetch orders", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<OrderResponse> call, Throwable t) {
                swipeRefresh.setRefreshing(false);
                Log.e("MyOrdersActivity", "Error: " + t.getMessage());
                Toast.makeText(MyOrdersActivity.this, "Network error", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void applyFilterAndGroup() {
        List<Order> filteredList = new ArrayList<>();
        if (currentFilter.equalsIgnoreCase("All")) {
            for (Order order : allOrdersList) {
                // Exclude hold but include declined as cancelled
                if (!order.getStatus().equalsIgnoreCase("hold")) {
                    filteredList.add(order);
                }
            }
        } else {
            for (Order order : allOrdersList) {
                if (order.getStatus().equalsIgnoreCase(currentFilter)) {
                    filteredList.add(order);
                }
            }
        }

        groupOrdersByDateTimeAndStatus(filteredList);
    }

    private void groupOrdersByDateTimeAndStatus(List<Order> orders) {
        Map<String, List<Order>> groups = new LinkedHashMap<>();
        for (Order order : orders) {
            // Grouping key should consider 'declined' and 'cancelled' as the same state for grouping
            String displayStatus = order.getStatus().equalsIgnoreCase("declined") ? "cancelled" : order.getStatus().toLowerCase();
            String groupKey = order.getCreatedAt() + "_" + displayStatus;

            if (!groups.containsKey(groupKey)) {
                groups.put(groupKey, new ArrayList<>());
            }
            groups.get(groupKey).add(order);
        }

        groupedOrders.clear();
        groupedOrders.addAll(groups.values());
        adapter.notifyDataSetChanged();
        
        updateEmptyState(groupedOrders.isEmpty());
    }

    private void updateEmptyState(boolean isEmpty) {
        if (isEmpty) {
            layoutEmpty.setVisibility(View.VISIBLE);
            rvOrders.setVisibility(View.GONE);
            
            String message = "No " + currentFilter.toLowerCase() + " orders found";
            if (currentFilter.equalsIgnoreCase("All")) {
                message = "You haven't placed any orders yet";
            }
            tvEmptyMessage.setText(message);
        } else {
            layoutEmpty.setVisibility(View.GONE);
            rvOrders.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onRefreshNeeded() {
        fetchOrders();
    }
}
