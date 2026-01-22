package com.bulkagrochemapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.ProductDetailActivity;
import com.bulkagrochemapp.R;
import com.bulkagrochemapp.model.Product;
import com.bulkagrochemapp.utils.HtmlUtil;
import com.bulkagrochemapp.utils.WishlistManager;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Random;

public class MostPopularProductAdapter
        extends RecyclerView.Adapter<MostPopularProductAdapter.ViewHolder> {

    private Context context;
    private List<Product> productList;
    private Random random = new Random();

    public MostPopularProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product_horizontal, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Product product = productList.get(position);
        if (product == null) return;

        // ===== Name =====
        holder.txtName.setText(product.getName());

        // ===== Price =====
        holder.txtPrice.setText("₹" + String.format("%.2f", product.getPrice())+"/"+product.getStocktype());

        // ===== Original Price =====
        if (product.getOriginalPrice() > product.getPrice()) {
            holder.txtOriginalPrice.setText(
                    "₹" + String.format("%.2f", product.getOriginalPrice())
            );
            holder.txtOriginalPrice.setPaintFlags(
                    holder.txtOriginalPrice.getPaintFlags()
                            | Paint.STRIKE_THRU_TEXT_FLAG
            );
            holder.txtOriginalPrice.setVisibility(View.VISIBLE);
        } else {
            holder.txtOriginalPrice.setVisibility(View.GONE);
        }

        // ===== Discount Badge =====
        if (product.getDiscountPercent() > 0) {
            holder.txtDiscount.setText(product.getDiscountPercent() + "% OFF");
            holder.txtDiscount.setVisibility(View.VISIBLE);
        } else {
            holder.txtDiscount.setVisibility(View.GONE);
        }

        // ===== Description =====
        if (product.getDescription() != null && !product.getDescription().isEmpty()) {
            holder.txtDescription.setText(
                    HtmlUtil.fromHtml(product.getDescription())
            );
            holder.txtDescription.setVisibility(View.VISIBLE);
        } else {
            holder.txtDescription.setVisibility(View.GONE);
        }

        // ===== Details =====
        if (product.getDetails() != null && !product.getDetails().isEmpty()) {
            holder.txtDetails.setText(
                    HtmlUtil.fromHtml(product.getDetails())
            );
            holder.txtDetails.setVisibility(View.VISIBLE);
        } else {
            holder.txtDetails.setVisibility(View.GONE);
        }

        // ===== Reviews & Rating =====
        int reviews = random.nextInt(91) + 10;
        holder.txtReviews.setText(reviews + " Reviews");
        holder.ratingBar.setRating(4.5f); // static or dynamic later

        // ===== Stock =====
        holder.txtStock.setText("InStock");
        holder.txtStock.setTextColor(
                ContextCompat.getColor(context, R.color.green)
        );

        // ===== Image (PICASSO) =====
        Picasso.get()
                .load(product.getImage()) // FULL URL FROM API
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.drawable.ic_image_error)
                .into(holder.imgProduct);

        // ===== Wishlist Logic =====
        updateWishlistIcon(holder.btnWishlist, product.getId());

        holder.btnWishlist.setOnClickListener(v -> {
            Animation scaleAnim = AnimationUtils.loadAnimation(context, R.anim.scale_up);
            holder.btnWishlist.startAnimation(scaleAnim);

            if (WishlistManager.isInWishlist(product.getId())) {
                WishlistManager.removeFromWishlistWithSync(context, product.getId());
                Toast.makeText(context, product.getName() + " removed from wishlist", Toast.LENGTH_SHORT).show();
            } else {
                WishlistManager.addToWishlistWithSync(context, product);
                Toast.makeText(context, product.getName() + " added to wishlist", Toast.LENGTH_SHORT).show();
            }
            updateWishlistIcon(holder.btnWishlist, product.getId());
        });

        // ===== Click (Button ONLY) =====
        View.OnClickListener openDetails = v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            intent.putExtra("product_name", product.getName());
            intent.putExtra("product_price", product.getPrice());
            intent.putExtra("product_stock", product.getStocktype());
            intent.putExtra("minimum_qty", product.getMinimumQty());
            intent.putExtra("product_image", product.getImage());
            intent.putExtra("product_description", product.getDescription());
            intent.putExtra("product_details", product.getDetails());
            intent.putExtra("product_original_price", product.getOriginalPrice());
            intent.putExtra("product_discount_percent", product.getDiscountPercent());
            
            // Pass the whole product object to access wholesale data
            intent.putExtra("product", product);

            context.startActivity(intent);
        };

        holder.ViewDetailBtn.setOnClickListener(openDetails);
        
        // Removed item click listener from holder.itemView to only allow button click
        holder.itemView.setClickable(false);
    }

    private void updateWishlistIcon(ImageButton btn, int productId) {
        if (WishlistManager.isInWishlist(productId)) {
            btn.setImageResource(R.drawable.ic_wishlist_filled);
        } else {
            btn.setImageResource(R.drawable.ic_wishlist_border);
        }
    }

    @Override
    public int getItemCount() {
        return productList != null ? productList.size() : 0;
    }

    // ===== ViewHolder =====
    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct;
        TextView txtName, txtPrice, txtOriginalPrice,
                txtDiscount, txtReviews, txtDescription,
                txtDetails, txtStock;
        RatingBar ratingBar;
        Button ViewDetailBtn;
        ImageButton btnWishlist;

        ViewHolder(@NonNull View itemView) {
            super(itemView);

            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtProductName);
            txtPrice = itemView.findViewById(R.id.txtProductPrice);
            txtOriginalPrice = itemView.findViewById(R.id.txtOriginalPrice);
            txtDiscount = itemView.findViewById(R.id.txtDiscount);
            txtReviews = itemView.findViewById(R.id.txtReviews);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtDetails = itemView.findViewById(R.id.txtDetails);
            txtStock = itemView.findViewById(R.id.txtStock);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            ViewDetailBtn = itemView.findViewById(R.id.ViewDetailBtn);
            btnWishlist = itemView.findViewById(R.id.btnWishlist);
        }
    }
}
