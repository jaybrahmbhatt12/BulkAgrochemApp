package com.bulkagrochemapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.CartActivity;
import com.bulkagrochemapp.ProductDetailActivity;
import com.bulkagrochemapp.R;
import com.bulkagrochemapp.model.CartItem;
import com.bulkagrochemapp.model.Product;
import com.bulkagrochemapp.utils.CartManager;
import com.bulkagrochemapp.utils.WishlistManager;
import com.google.android.material.snackbar.Snackbar;
import com.squareup.picasso.Picasso;

import java.util.List;

public class WishlistAdapter extends RecyclerView.Adapter<WishlistAdapter.ViewHolder> {

    private Context context;
    private List<Product> wishlist;
    private Runnable onListChanged;

    public WishlistAdapter(Context context, List<Product> wishlist, Runnable onListChanged) {
        this.context = context;
        this.wishlist = wishlist;
        this.onListChanged = onListChanged;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_wishlist, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = wishlist.get(position);

        holder.txtName.setText(product.getName());
        String priceText = "₹" + String.format("%.2f", product.getPrice());
        if (product.getStocktype() != null && !product.getStocktype().isEmpty()) {
            priceText += "/" + product.getStocktype();
        }
        holder.txtPrice.setText(priceText);
        Picasso.get().load(product.getImage()).placeholder(R.drawable.ic_launcher_foreground).into(holder.imgProduct);

        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product", product);
            context.startActivity(intent);
        });

        holder.btnViewMore.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product", product);
            context.startActivity(intent);
        });

        holder.btnRemove.setOnClickListener(v -> {
            WishlistManager.removeFromWishlistWithSync(context, product.getId());
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                wishlist.remove(currentPos);
                notifyItemRemoved(currentPos);
                onListChanged.run();
                Toast.makeText(context, "Removed from wishlist", Toast.LENGTH_SHORT).show();
            }
        });

        holder.btnAddToCart.setOnClickListener(v -> {
            // Create CartItem with minimum quantity
            CartItem cartItem = new CartItem(
                    product.getId(),
                    product.getName(),
                    product.getImage(),
                    product.getPrice(),
                    product.getMinimumQty(),
                    product.getStocktype(),
                    product.getTaxRate()
            );
            cartItem.setQuantity(product.getMinimumQty());

            // Add to Cart
            CartManager.addToCart(context, cartItem);

            // Remove from Wishlist
            WishlistManager.removeFromWishlistWithSync(context, product.getId());
            int currentPos = holder.getAdapterPosition();
            if (currentPos != RecyclerView.NO_POSITION) {
                wishlist.remove(currentPos);
                notifyItemRemoved(currentPos);
                onListChanged.run();
            }

            // Show Snackbar with "Go to Cart" action
            Snackbar.make(v, product.getName() + " added to cart", Snackbar.LENGTH_LONG)
                    .setAction("GO TO CART", view -> {
                        Intent intent = new Intent(context, CartActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                        intent.putExtra("flag", true);
                        context.startActivity(intent);
                    }).show();
        });
    }

    @Override
    public int getItemCount() {
        return wishlist.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtName, txtPrice;
        ImageButton btnRemove;
        Button btnAddToCart, btnViewMore;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtName);
            txtPrice = itemView.findViewById(R.id.txtPrice);
            btnRemove = itemView.findViewById(R.id.btnRemove);
            btnAddToCart = itemView.findViewById(R.id.btnAddToCart);
            btnViewMore = itemView.findViewById(R.id.btnViewMore);
        }
    }
}