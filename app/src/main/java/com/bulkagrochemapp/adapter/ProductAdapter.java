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
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.ProductDetailActivity;
import com.bulkagrochemapp.R;
import com.bulkagrochemapp.model.Product;
import com.bulkagrochemapp.utils.HtmlUtil;
import com.bulkagrochemapp.utils.WishlistManager;
import com.squareup.picasso.Picasso;

import java.util.List;

//=========== Product Adapter ===========//

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder>
        implements Filterable {

    Context context;
    List<Product> productList;
    private final OnProductClickListener onProductClickListener;
    List<Product> productListFull; // original list
    private Random random = new Random(); // Initialize random object

    //============== Constructor ================//

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> productList, OnProductClickListener onProductClickListener) {
        this.context = context;
        this.productList = productList;
        this.productListFull = new ArrayList<>(productList); // copy
        this.onProductClickListener = onProductClickListener;
    }

    //====== onCreateViewHolder() =====//

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    //====== onBindViewHolder() =====//

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Product product = productList.get(position);

        holder.txtName.setText(product.getName());
        holder.txtPrice.setText(
                "₹" + String.format("%.2f", product.getPrice()) + "/" + product.getStocktype()
        );
        
        // ===== Original Price =====
        if (product.getOriginalPrice() > product.getPrice()) {
            holder.txtOriginalPrice.setText("₹" + String.format("%.2f", product.getOriginalPrice()));
            holder.txtOriginalPrice.setPaintFlags(holder.txtOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
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


        if (product.getDescription() != null) {
            holder.txtDescription.setText(HtmlUtil.fromHtml(product.getDescription()));
        } else {
            holder.txtDescription.setText("");
        }

        // Bind details
        if (product.getDetails() != null && !product.getDetails().isEmpty()) {
            holder.txtDetails.setText(HtmlUtil.fromHtml(product.getDetails()));
            holder.txtDetails.setVisibility(View.VISIBLE);
        } else {
            holder.txtDetails.setVisibility(View.GONE);
        }


        // Generate random reviews count between 10 and 100
        int reviewsCount = random.nextInt(91) + 10;
        holder.txtReviews.setText(reviewsCount + " Reviews");


        // The API provides the full URL, so use it directly.
        String imageUrl = product.getImage();

        Picasso.get()
                .load(imageUrl)
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

        // Add the click listener to the View More button
        holder.ViewDetailBtn.setOnClickListener(v -> {

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
        });

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
        return productList.size();
    }

    // SEARCH FILTER IMPLEMENTATION
    @Override
    public Filter getFilter() {
        return productFilter;
    }

    // Filter implementation

    private final Filter productFilter = new Filter() {
        @Override
        protected FilterResults performFiltering(CharSequence constraint) {

            List<Product> filteredList = new ArrayList<>();

            if (constraint == null || constraint.length() == 0) {
                filteredList.addAll(productListFull);
            } else {
                String filterPattern =
                        constraint.toString().toLowerCase().trim();

                for (Product product : productListFull) {
                    if (product.getName().toLowerCase().contains(filterPattern)) {
                        filteredList.add(product);
                    }
                }
            }

            FilterResults results = new FilterResults();
            results.values = filteredList;
            return results;
        }

        //====== publishResults() =====//

        @Override
        protected void publishResults(CharSequence constraint, FilterResults results) {
            productList.clear();
            productList.addAll((List<Product>) results.values);
            notifyDataSetChanged();
        }
    };

    //====== ViewHolder =====//

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct;
        TextView txtName, txtPrice, txtReviews, txtDescription, txtDetails, txtOriginalPrice, txtDiscount;
        Button ViewDetailBtn;
        ImageButton btnWishlist;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtProductName);
            txtPrice = itemView.findViewById(R.id.txtProductPrice);
            txtReviews = itemView.findViewById(R.id.txtReviews);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtDetails = itemView.findViewById(R.id.txtDetails);
            ViewDetailBtn = itemView.findViewById(R.id.ViewDetailBtn);
            txtOriginalPrice = itemView.findViewById(R.id.txtOriginalPrice);
            txtDiscount = itemView.findViewById(R.id.txtDiscount);
            btnWishlist = itemView.findViewById(R.id.btnWishlist);
        }
    }
}
