package com.bulkagrochemapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import java.util.Random;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.ProductDetailActivity;
import com.bulkagrochemapp.R;
import com.bulkagrochemapp.model.Product;
import com.bulkagrochemapp.utils.HtmlUtil;
import com.squareup.picasso.Picasso;

import java.util.List;

//=========== Trending Product Adapter ===========//

public class TrendingProductAdapter extends RecyclerView.Adapter<TrendingProductAdapter.ViewHolder> {

    Context context;
    List<Product> productList;
    private Random random = new Random();

    //============== Constructor ================//

    public TrendingProductAdapter(Context context, List<Product> productList) {
        this.context = context;
        this.productList = productList;
    }

    //====== onCreateViewHolder() =====//

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Use the horizontal layout for trending products
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product_horizontal, parent, false);
        return new ViewHolder(view);
    }

    //====== onBindViewHolder() =====//

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = productList.get(position);

        holder.txtName.setText(product.getName());
        holder.txtPrice.setText("₹" + String.format("%.2f", product.getPrice()));

        if (product.getOriginalPrice() > 0) {
            holder.txtOriginalPrice.setText("₹" + String.format("%.2f", product.getOriginalPrice()));
            holder.txtOriginalPrice.setPaintFlags(holder.txtOriginalPrice.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.txtOriginalPrice.setVisibility(View.VISIBLE);
        } else {
            holder.txtOriginalPrice.setVisibility(View.GONE);
        }

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

        // Remove the click listener from the whole item
        holder.itemView.setOnClickListener(null);
        holder.itemView.setClickable(false);
        holder.itemView.setFocusable(false);

        // Add the click listener to the View More button
        holder.ViewDetailBtn.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product_id", product.getId());
            intent.putExtra("product_name", product.getName());
            intent.putExtra("product_price", product.getPrice());
            intent.putExtra("product_image", product.getImage());
            intent.putExtra("product_description", product.getDescription());
            intent.putExtra("product_details", product.getDetails());
            // Optionally pass original price and discount if needed in DetailActivity
            intent.putExtra("product_original_price", product.getOriginalPrice());
            intent.putExtra("product_discount_percent", product.getDiscountPercent());

            context.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct;
        TextView txtName, txtPrice, txtOriginalPrice, txtDiscount, txtReviews, txtDescription, txtDetails, txtStock;
        Button ViewDetailBtn;
        RatingBar ratingBar;

        public ViewHolder(@NonNull View itemView) {
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
        }
    }
}
