package com.bulkagrochemapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.R;
import com.bulkagrochemapp.model.Product;

import java.util.List;

public class SearchSuggestionAdapter extends RecyclerView.Adapter<SearchSuggestionAdapter.SuggestionViewHolder> {

    private final Context context;
    private final List<Product> suggestionList;
    private final OnSuggestionClickListener listener;

    public interface OnSuggestionClickListener {
        void onSuggestionClick(Product product);
    }

    public SearchSuggestionAdapter(Context context, List<Product> suggestionList, OnSuggestionClickListener listener) {
        this.context = context;
        this.suggestionList = suggestionList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public SuggestionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_search_suggestion, parent, false);
        return new SuggestionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SuggestionViewHolder holder, int position) {
        Product product = suggestionList.get(position);
        holder.productName.setText(product.getName());
        holder.itemView.setOnClickListener(v -> listener.onSuggestionClick(product));
    }

    @Override
    public int getItemCount() {
        return suggestionList.size();
    }

    static class SuggestionViewHolder extends RecyclerView.ViewHolder {
        TextView productName;

        public SuggestionViewHolder(@NonNull View itemView) {
            super(itemView);
            productName = itemView.findViewById(R.id.product_name);
        }
    }
}
