package com.bulkagrochemapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.R;
import com.bulkagrochemapp.model.CartItem;
import com.bulkagrochemapp.model.Order;

import java.util.List;
import java.util.Locale;

public class OrderAdapter extends RecyclerView.Adapter<OrderAdapter.OrderViewHolder> {

    private Context context;
    private List<Order> orderList;
    private OnOrderActionListener listener;

    public interface OnOrderActionListener {
        void onReorder(Order order);
        void onTrack(Order order);
        void onCancel(Order order);
        void onReturn(Order order);
        void onInvoice(Order order);
    }

    public OrderAdapter(Context context, List<Order> orderList, OnOrderActionListener listener) {
        this.context = context;
        this.orderList = orderList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public OrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order, parent, false);
        return new OrderViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OrderViewHolder holder, int position) {
        Order order = orderList.get(position);

        holder.txtOrderId.setText("Order #" + order.getId());
        holder.txtOrderDate.setText("Placed on " + order.getDate());
        holder.txtTotalAmount.setText(String.format(Locale.getDefault(), "₹%.2f", order.getTotal()));
        holder.txtOrderStatus.setText(order.getStatus());

        // Update status color and button visibility
        String status = order.getStatus() != null ? order.getStatus() : "";
        
        // Invoice button visibility: only for pending, processing, completed
        if (status.equalsIgnoreCase("Pending") || 
            status.equalsIgnoreCase("Processing") || 
            status.equalsIgnoreCase("Completed") ||
            status.equalsIgnoreCase("Delivered")) { // Delivered usually maps to completed in some systems
            holder.btnInvoice.setVisibility(View.VISIBLE);
        } else {
            holder.btnInvoice.setVisibility(View.GONE);
        }

        if ("Delivered".equalsIgnoreCase(status) || "Completed".equalsIgnoreCase(status)) {
            holder.txtOrderStatus.setTextColor(context.getResources().getColor(android.R.color.holo_green_dark));
            holder.btnReturn.setVisibility(View.VISIBLE);
            holder.btnCancel.setVisibility(View.GONE);
            holder.btnTrack.setVisibility(View.VISIBLE);
        } else if ("Cancelled".equalsIgnoreCase(status)) {
            holder.txtOrderStatus.setTextColor(context.getResources().getColor(android.R.color.holo_red_dark));
            holder.btnReturn.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.GONE);
            holder.btnTrack.setVisibility(View.GONE);
        } else {
            holder.txtOrderStatus.setTextColor(context.getResources().getColor(R.color.orange_brand));
            holder.btnReturn.setVisibility(View.GONE);
            holder.btnCancel.setVisibility(View.VISIBLE);
            holder.btnTrack.setVisibility(View.VISIBLE);
        }

        // Add items dynamically
        holder.layoutItems.removeAllViews();
        if (order.getItems() != null) {
            for (CartItem item : order.getItems()) {
                TextView tv = new TextView(context);
                tv.setText(item.getQuantity() + " x " + item.getName());
                tv.setTextSize(14);
                tv.setTextColor(context.getResources().getColor(android.R.color.darker_gray));
                tv.setPadding(0, 4, 0, 4);
                holder.layoutItems.addView(tv);
            }
        }

        holder.btnReorder.setOnClickListener(v -> listener.onReorder(order));
        holder.btnTrack.setOnClickListener(v -> listener.onTrack(order));
        holder.btnCancel.setOnClickListener(v -> listener.onCancel(order));
        holder.btnReturn.setOnClickListener(v -> listener.onReturn(order));
        holder.btnInvoice.setOnClickListener(v -> listener.onInvoice(order));
    }

    @Override
    public int getItemCount() {
        return orderList.size();
    }

    public static class OrderViewHolder extends RecyclerView.ViewHolder {
        TextView txtOrderId, txtOrderStatus, txtOrderDate, txtTotalAmount;
        LinearLayout layoutItems;
        Button btnReorder, btnTrack, btnCancel, btnReturn, btnInvoice;

        public OrderViewHolder(@NonNull View itemView) {
            super(itemView);
            txtOrderId = itemView.findViewById(R.id.txtOrderId);
            txtOrderStatus = itemView.findViewById(R.id.txtOrderStatus);
            txtOrderDate = itemView.findViewById(R.id.txtOrderDate);
            txtTotalAmount = itemView.findViewById(R.id.txtTotalAmount);
            layoutItems = itemView.findViewById(R.id.layoutItems);
            btnReorder = itemView.findViewById(R.id.btnReorder);
            btnTrack = itemView.findViewById(R.id.btnTrack);
            btnCancel = itemView.findViewById(R.id.btnCancel);
            btnReturn = itemView.findViewById(R.id.btnReturn);
            btnInvoice = itemView.findViewById(R.id.btnInvoice);
        }
    }
}
