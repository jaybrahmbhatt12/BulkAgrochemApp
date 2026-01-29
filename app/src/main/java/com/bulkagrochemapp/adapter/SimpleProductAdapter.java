package com.bulkagrochemapp.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.ProductDetailActivity;
import com.bulkagrochemapp.R;
import com.bulkagrochemapp.model.Product;
import com.squareup.picasso.Picasso;

import java.util.List;

public class SimpleProductAdapter extends RecyclerView.Adapter<SimpleProductAdapter.ViewHolder> {

    private Context context;
    private List<Product> list;

    public SimpleProductAdapter(Context context, List<Product> list) {
        this.context = context;
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_product_list_simple, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Product product = list.get(position);

        holder.txtProductName.setText(product.getName());

        Picasso.get()
                .load(product.getImage())
                .placeholder(R.drawable.ic_launcher_foreground)
                .into(holder.imgProduct);

        holder.btnView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ProductDetailActivity.class);
            intent.putExtra("product", product);
            intent.putExtra("product_id", product.getId());
            intent.putExtra("product_name", product.getName());
            intent.putExtra("product_image", product.getImage());
            intent.putExtra("product_price", product.getPrice());
            intent.putExtra("product_stock", product.getStocktype());
            intent.putExtra("minimum_qty", product.getMinimumQty());
            context.startActivity(intent);
        });

        holder.itemView.setOnClickListener(v -> holder.btnView.performClick());
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProduct;
        TextView txtProductName;
        Button btnView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProduct = itemView.findViewById(R.id.imgProduct);
            txtProductName = itemView.findViewById(R.id.txtProductName);
            btnView = itemView.findViewById(R.id.btnView);
        }
    }
}