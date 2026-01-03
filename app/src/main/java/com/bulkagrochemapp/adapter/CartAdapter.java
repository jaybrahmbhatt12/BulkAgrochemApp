package com.bulkagrochemapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.R;
import com.bulkagrochemapp.model.CartItem;
import com.bulkagrochemapp.utils.CartManager;
import com.squareup.picasso.Picasso;

import java.util.List;

//=========== Cart Adapter ===========//

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ViewHolder> {

    Context context;
    List<CartItem> cartList;
    Runnable totalUpdater;

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
        holder.txtPrice.setText("₹" + item.getTotalPrice());

        Picasso.get()
                .load(item.getImage())
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.imgProduct);

        // ➕ Increase Quantity
        holder.btnPlus.setOnClickListener(v -> {
            item.increaseQty();
            notifyItemChanged(position);
            totalUpdater.run();
        });

        // ➖ Decrease Quantity
        holder.btnMinus.setOnClickListener(v -> {
            item.decreaseQty();

            if (item.getQuantity() <= 0) {
                cartList.remove(position);
                notifyItemRemoved(position);
                notifyItemRangeChanged(position, cartList.size());
            } else {
                notifyItemChanged(position);
            }

            totalUpdater.run();
        });

    }


    @Override
    public int getItemCount() {
        return cartList.size();
    }

    //====== ViewHolder =====//

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct;
        TextView txtName, txtQty, txtPrice;
        TextView btnPlus, btnMinus;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            txtQty = itemView.findViewById(R.id.txtQty);
            btnPlus = itemView.findViewById(R.id.btnPlus);
            btnMinus = itemView.findViewById(R.id.btnMinus);
        }
    }

}
