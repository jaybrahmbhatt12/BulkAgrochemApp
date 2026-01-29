package com.bulkagrochemapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.R;
import com.bulkagrochemapp.model.Category;
import com.bulkagrochemapp.model.Product;

import java.util.List;
import java.util.Map;

public class AllProductsCategoryAdapter extends RecyclerView.Adapter<AllProductsCategoryAdapter.ViewHolder> {

    private Context context;
    private List<Category> categories;
    private Map<Integer, List<Product>> categoryProductMap;

    public AllProductsCategoryAdapter(Context context, List<Category> categories, Map<Integer, List<Product>> categoryProductMap) {
        this.context = context;
        this.categories = categories;
        this.categoryProductMap = categoryProductMap;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_category_with_products, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Category category = categories.get(position);
        holder.txtCategoryTitle.setText(category.getName());

        List<Product> products = categoryProductMap.get(category.getId());
        if (products != null) {
            holder.rvProductsInCategory.setLayoutManager(new LinearLayoutManager(context));
            holder.rvProductsInCategory.setAdapter(new SimpleProductAdapter(context, products));
        }
    }

    @Override
    public int getItemCount() {
        return categories.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtCategoryTitle;
        RecyclerView rvProductsInCategory;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCategoryTitle = itemView.findViewById(R.id.txtCategoryTitle);
            rvProductsInCategory = itemView.findViewById(R.id.rvProductsInCategory);
        }
    }
}