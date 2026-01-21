package com.bulkagrochemapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.ProductDetailActivity;
import com.bulkagrochemapp.R;
import com.bulkagrochemapp.model.CartItem;
import com.bulkagrochemapp.utils.CartManager;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Locale;

//=========== Cart Adapter ===========//

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    Context context;
    List<CartItem> cartList;
    Runnable totalUpdater;

    private boolean flag=false;

    public CartAdapter(Context context, List<CartItem> cartList, Runnable totalUpdater) {
        this.context = context;
        this.cartList = cartList;
        this.totalUpdater = totalUpdater;
    }

    //====== onCreateViewHolder() =====//

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_cart, parent, false);
        return new ViewHolder(view);
    }

    //====== onBindViewHolder() =====//

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        CartItem item = cartList.get(position);

        holder.txtName.setText(item.getName());
        holder.txtQty.setText(String.valueOf(item.getQuantity()));
        
        holder.txtPricePerUnit.setText(String.format(Locale.getDefault(), "Price: ₹%.2f", item.getPrice()));
        holder.txtLineTotal.setText(String.format(Locale.getDefault(), "₹%.2f", item.getTotalAmount()));
        holder.txtTaxLabel.setText(String.format(Locale.getDefault(), "Tax (%.0f%%)", item.getTaxRate()));
        holder.txtTaxAmount.setText(String.format(Locale.getDefault(), "₹%.2f", item.getTaxAmount()));
        holder.txtSubtotal.setText(String.format(Locale.getDefault(), "₹%.2f", item.getSubtotalAmount()));


        String imageUrl = item.getImage();
        if (imageUrl != null && !imageUrl.trim().isEmpty()) {
            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.drawable.ic_launcher_foreground)
                    .into(holder.imgProduct);
        } else {
            holder.imgProduct.setImageResource(R.drawable.ic_launcher_foreground);
        }

        // Redirect to ProductDetailActivity on click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product_id", item.getProductId());
            intent.putExtra("product_name", item.getName());
            intent.putExtra("product_image", item.getImage());
            intent.putExtra("product_price", item.getPrice());
            intent.putExtra("product_stock", item.getStockType());
            intent.putExtra("minimum_qty", item.getMinQuantity());
            intent.putExtra("is_from_cart", true); // Pass this flag to hide buttons
            context.startActivity(intent);
        });

        // ➕ Increase Quantity
        holder.btnPlus.setOnClickListener(v -> {
            item.increaseQty();
            notifyItemChanged(holder.getAdapterPosition());
            totalUpdater.run();
            CartManager.updateCartItem(context, item);
        });

        // ➖ Decrease Quantity
        holder.btnMinus.setOnClickListener(v -> {
            item.decreaseQty();

            int currentPos = holder.getAdapterPosition();
            if (item.getQuantity() <= 0) {
                // Reset to minimum quantity so that if UNDO is clicked, 
                // the item returns with its base minimum quantity instead of 0.
                item.setQuantity(item.getMinQuantity());
                removeItemWithUndo(holder.itemView, currentPos);
            } else {
                notifyItemChanged(currentPos);
                totalUpdater.run();
                CartManager.updateCartItem(context, item);
            }
        });

        // 🗑️ Remove Button
        holder.btnRemove.setOnClickListener(v -> {
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                removeItemWithUndo(v, currentPos);
            }
        });

    }

    private void removeItemWithUndo(View view, int position) {
        CartItem deletedItem = cartList.get(position);
        int deletedPosition = position;

        // Temporarily remove
        cartList.remove(position);
        notifyItemRemoved(position);
        notifyItemRangeChanged(position, cartList.size());
        totalUpdater.run();

        // Note: For full production, you'd likely want a delete API call here too.
        // But since the user only provided add and fetch, we'll stick to those.

        // Show Snackbar with Undo option
        Snackbar.make(view, deletedItem.getName() + " removed", 5000)
                .setAction("UNDO", v -> {
                    cartList.add(deletedPosition, deletedItem);
                    notifyItemInserted(deletedPosition);
                    notifyItemRangeChanged(deletedPosition, cartList.size());
                    totalUpdater.run();
                    CartManager.updateCartItem(context, deletedItem);
                }).show();
    }


    @Override
    public int getItemCount() {
        return cartList.size();
    }

    //====== ViewHolder =====//

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct;
        TextView txtName, txtQty, txtPricePerUnit, txtLineTotal, txtTaxLabel, txtTaxAmount, txtSubtotal;
        TextView btnPlus, btnMinus;
        ImageButton btnRemove;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtName);
            txtPricePerUnit = itemView.findViewById(R.id.txtPricePerUnit);
            txtLineTotal = itemView.findViewById(R.id.txtLineTotal);
            txtTaxLabel = itemView.findViewById(R.id.txtTaxLabel);
            txtTaxAmount = itemView.findViewById(R.id.txtTaxAmount);
            txtSubtotal = itemView.findViewById(R.id.txtSubtotal);
            txtQty = itemView.findViewById(R.id.txtQty);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
            btnRemove = itemView.findViewById(R.id.btnRemove);
        }
    }

}
