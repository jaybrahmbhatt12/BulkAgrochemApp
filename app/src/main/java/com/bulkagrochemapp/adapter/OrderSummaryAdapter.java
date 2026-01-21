package com.bulkagrochemapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bulkagrochemapp.R;
import com.bulkagrochemapp.model.CartItem;

import java.util.List;
import java.util.Locale;

public class OrderSummaryAdapter extends RecyclerView.Adapter<OrderSummaryAdapter.ViewHolder> {

    private final Context context;
    private final List<CartItem> cartItems;

    public OrderSummaryAdapter(Context context, List<CartItem> cartItems) {
        this.context = context;
        this.cartItems = cartItems;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_order_summary_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        CartItem item = cartItems.get(position);

        holder.txtName.setText(item.getProduct().getName());
        holder.txtQty.setText("Quantity: " + item.getQuantity());
        
        double lineTotal = item.getProduct().getPrice() * item.getQuantity();
        holder.txtPrice.setText(String.format(Locale.getDefault(), "₹%.2f", lineTotal));

        Glide.with(context)
                .load(item.getProduct().getImage())
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.imgProduct);
    }

    @Override
    public int getItemCount() {
        return cartItems.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName, txtQty, txtPrice;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtName);
            txtQty = itemView.findViewById(R.id.txtQty);
            txtPrice = itemView.findViewById(R.id.txtPrice);
        }
    }
}
