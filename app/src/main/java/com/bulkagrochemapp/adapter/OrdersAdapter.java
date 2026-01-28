package com.bulkagrochemapp.adapter;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bulkagrochemapp.MainActivity;
import com.bulkagrochemapp.R;
import com.bulkagrochemapp.model.Order;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;
import com.bulkagrochemapp.utils.InvoiceGenerator;
import com.google.android.material.button.MaterialButton;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.ViewHolder> {

    private final Context context;
    private final List<List<Order>> groupedOrders;
    private final OnOrderActionListener actionListener;

    public interface OnOrderActionListener {
        void onRefreshNeeded();
    }

    public OrdersAdapter(Context context, List<List<Order>> groupedOrders, OnOrderActionListener actionListener) {
        this.context = context;
        this.groupedOrders = groupedOrders;
        this.actionListener = actionListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_card, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        List<Order> orderGroup = groupedOrders.get(position);
        if (orderGroup == null || orderGroup.isEmpty()) return;

        Order mainOrder = orderGroup.get(0);
        String status = mainOrder.getStatus() != null ? mainOrder.getStatus().toLowerCase() : "";
        
        if (status.equals("declined")) {
            holder.txtStatus.setText("cancelled");
        } else {
            holder.txtStatus.setText(status);
        }
        
        int dotColor = Color.GRAY;
        switch (status) {
            case "pending": dotColor = Color.parseColor("#FFC107"); break;
            case "processing": dotColor = Color.parseColor("#2196F3"); break;
            case "completed": dotColor = Color.parseColor("#4CAF50"); break;
            case "cancelled":
            case "declined":
                dotColor = Color.RED;
                break;
        }
        GradientDrawable shape = new GradientDrawable();
        shape.setShape(GradientDrawable.OVAL);
        shape.setColor(dotColor);
        holder.viewStatusDot.setBackground(shape);

        String fullDateTime = mainOrder.getCreatedAt();
        if (fullDateTime != null && fullDateTime.contains(" ")) {
            holder.txtDate.setText(fullDateTime.split(" ")[0]);
        } else {
            holder.txtDate.setText(fullDateTime);
        }

        holder.txtAddress.setText("Address: " + mainOrder.getCustomerAddress() + ", " + mainOrder.getCustomerCity());
        
        double grandTotal = 0;
        int totalQty = 0;

        holder.productsContainer.removeAllViews();
        LayoutInflater inflater = LayoutInflater.from(context);

        List<Integer> orderIds = new ArrayList<>();
        int displayedProductCount = 0;
        final int MAX_DISPLAYED_PRODUCTS = 2;
        boolean hasMoreProducts = false;

        for (int i = 0; i < orderGroup.size(); i++) {
            Order order = orderGroup.get(i);
            orderIds.add(order.getId());
            grandTotal += order.getPayAmount();
            try {
                totalQty += Integer.parseInt(order.getTotalQty());
            } catch (Exception ignored) {}

            try {
                JSONArray cartArray = new JSONArray(order.getCart());
                for (int j = 0; j < cartArray.length(); j++) {
                    if (displayedProductCount < MAX_DISPLAYED_PRODUCTS) {
                        JSONObject cartObj = cartArray.getJSONObject(j);
                        View productView = inflater.inflate(R.layout.item_order_product, holder.productsContainer, false);
                        setupProductView(productView, cartObj);
                        holder.productsContainer.addView(productView);
                        displayedProductCount++;
                    } else {
                        hasMoreProducts = true;
                    }
                }
            } catch (JSONException e) {
                Log.e("OrdersAdapter", "Error: " + e.getMessage());
            }
        }

        if (hasMoreProducts) {
            holder.btnViewAll.setVisibility(View.VISIBLE);
            holder.btnViewAll.setOnClickListener(v -> showAllProductsDialog(orderGroup));
        } else {
            holder.btnViewAll.setVisibility(View.GONE);
        }

        holder.txtTotalQty.setText("Total Quantity: " + totalQty);
        holder.txtTotalPrice.setText("Grand Total: ₹" + String.format(Locale.getDefault(), "%.2f", grandTotal));

        holder.btnPrimary.setVisibility(View.GONE);
        holder.btnSecondary.setVisibility(View.GONE);
        holder.btnInvoice.setVisibility(View.GONE);

        if (status.equals("pending") || status.equals("processing") || status.equals("completed")) {
            holder.btnInvoice.setVisibility(View.VISIBLE);
            holder.btnInvoice.setOnClickListener(v -> InvoiceGenerator.generateInvoice(context, mainOrder));
        }

        if (status.equals("pending")) {
            holder.btnPrimary.setVisibility(View.VISIBLE);
            holder.btnPrimary.setText("CANCEL ORDER");
            holder.btnPrimary.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.red)));
            holder.btnPrimary.setOnClickListener(v -> showConfirmationDialog("CANCEL", orderIds));
        } else if (status.equals("completed")) {
            holder.btnPrimary.setVisibility(View.VISIBLE);
            holder.btnPrimary.setText("REORDER");
            holder.btnPrimary.setBackgroundTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.green)));
            holder.btnPrimary.setOnClickListener(v -> reorder());
        }
    }

    private void setupProductView(View view, JSONObject cartObj) {
        ImageView imgProduct = view.findViewById(R.id.imgProduct);
        TextView txtName = view.findViewById(R.id.txtProductName);
        txtName.setText(cartObj.optString("name"));
        String imgUrl = cartObj.optString("image");
        if (!imgUrl.startsWith("http")) {
            imgUrl = ApiService.IMAGE_URL + imgUrl;
        }
        Glide.with(context).load(imgUrl).placeholder(R.drawable.ic_quick_order).into(imgProduct);
    }

    private void showAllProductsDialog(List<Order> orderGroup) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_order_products);
        if (dialog.getWindow() != null) {
            // Set width to 90% of screen to make it look like a popup
            int width = (int)(context.getResources().getDisplayMetrics().widthPixels * 0.90);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
            dialog.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
        }

        LinearLayout container = dialog.findViewById(R.id.productsContainer);
        View btnClose = dialog.findViewById(R.id.btnClose);
        btnClose.setOnClickListener(v -> dialog.dismiss());

        LayoutInflater inflater = LayoutInflater.from(context);
        for (Order order : orderGroup) {
            try {
                JSONArray cartArray = new JSONArray(order.getCart());
                for (int i = 0; i < cartArray.length(); i++) {
                    JSONObject cartObj = cartArray.getJSONObject(i);
                    View productView = inflater.inflate(R.layout.item_order_product, container, false);
                    setupProductView(productView, cartObj);
                    container.addView(productView);

                    View divider = new View(context);
                    divider.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 1));
                    divider.setBackgroundColor(Color.parseColor("#EEEEEE"));
                    container.addView(divider);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        dialog.show();
    }

    private void showConfirmationDialog(String action, List<Integer> orderIds) {
        new AlertDialog.Builder(context)
                .setTitle("Confirm " + action)
                .setMessage("Are you sure you want to " + action.toLowerCase() + " this entire order?")
                .setPositiveButton("PROCEED", (dialog, which) -> cancelAllOrders(orderIds))
                .setNegativeButton("CANCEL", null)
                .show();
    }

    private void cancelAllOrders(List<Integer> orderIds) {
        SharedPreferences prefs = context.getSharedPreferences("user_session", Context.MODE_PRIVATE);
        String userId = prefs.getString("id", null);

        if (userId == null) {
            Toast.makeText(context, "Session expired. Please login again.", Toast.LENGTH_SHORT).show();
            return;
        }

        final int[] completedCount = {0};
        final int totalToCancel = orderIds.size();
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        for (Integer orderId : orderIds) {
            apiService.cancelOrder(orderId, userId).enqueue(new Callback<ResponseBody>() {
                @Override
                public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                    completedCount[0]++;
                    if (completedCount[0] == totalToCancel) {
                        Toast.makeText(context, "Order cancelled successfully", Toast.LENGTH_SHORT).show();
                        if (actionListener != null) {
                            actionListener.onRefreshNeeded();
                        }
                    }
                }

                @Override
                public void onFailure(Call<ResponseBody> call, Throwable t) {
                    completedCount[0]++;
                    if (completedCount[0] == totalToCancel) {
                        Toast.makeText(context, "Order cancelled with some errors", Toast.LENGTH_SHORT).show();
                        if (actionListener != null) {
                            actionListener.onRefreshNeeded();
                        }
                    }
                }
            });
        }
    }

    private void reorder() {
        Toast.makeText(context, "Redirecting to Home for Reorder", Toast.LENGTH_SHORT).show();
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return groupedOrders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtStatus, txtDate, txtAddress, txtTotalQty, txtTotalPrice, btnViewAll;
        View viewStatusDot;
        LinearLayout productsContainer;
        MaterialButton btnPrimary, btnSecondary, btnInvoice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtStatus = itemView.findViewById(R.id.txtStatus);
            viewStatusDot = itemView.findViewById(R.id.viewStatusDot);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtAddress = itemView.findViewById(R.id.txtAddress);
            txtTotalQty = itemView.findViewById(R.id.txtTotalQty);
            txtTotalPrice = itemView.findViewById(R.id.txtTotalPrice);
            productsContainer = itemView.findViewById(R.id.productsContainer);
            btnPrimary = itemView.findViewById(R.id.btnPrimary);
            btnSecondary = itemView.findViewById(R.id.btnSecondary);
            btnInvoice = itemView.findViewById(R.id.btnInvoice);
            btnViewAll = itemView.findViewById(R.id.btnViewAll);
        }
    }
}
