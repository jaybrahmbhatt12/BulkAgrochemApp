package com.bulkagrochemapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.ImageView;
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

public class ProductAdapter extends RecyclerView.Adapter<ProductAdapter.ViewHolder> {

    private final Context context;
    private final List<Product> productList;
    private final OnProductClickListener onProductClickListener;
    List<Product> productListFull; // 🔹 original list
    private Random random = new Random(); // Initialize random object

    public interface OnProductClickListener {
        void onProductClick(Product product);
    }

    public ProductAdapter(Context context, List<Product> productList, OnProductClickListener onProductClickListener) {
        this.context = context;
        this.productList = productList;
        this.onProductClickListener = onProductClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context)
                .inflate(R.layout.item_product, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {

        Product product = productList.get(position);

        holder.txtName.setText(product.getName());
        holder.txtPrice.setText(
                "₹" + String.format("%.2f", product.getPrice())
        );

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
                .error(R.drawable.ic_launcher_foreground)
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

            context.startActivity(intent);
        });

    }

    @Override
    public int getItemCount() {
        return productList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {

        ImageView imgProduct;
        TextView txtName, txtPrice, txtReviews, txtDescription, txtDetails;
        Button ViewDetailBtn;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtName = itemView.findViewById(R.id.txtProductName);
            txtPrice = itemView.findViewById(R.id.txtProductPrice);
            txtReviews = itemView.findViewById(R.id.txtReviews);
            txtDescription = itemView.findViewById(R.id.txtDescription);
            txtDetails = itemView.findViewById(R.id.txtDetails);
            ViewDetailBtn = itemView.findViewById(R.id.ViewDetailBtn);

        }
    }
}
