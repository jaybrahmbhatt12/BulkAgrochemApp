package com.bulkagrochemapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bulkagrochemapp.adapter.WishlistAdapter;
import com.bulkagrochemapp.utils.WishlistManager;

public class WishlistActivity extends BaseActivity {

    private ImageButton btnBack;
    private TextView txtEmptyWishlist;
    private RecyclerView rvWishlist;
    private WishlistAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        btnBack = findViewById(R.id.btnBack);
        txtEmptyWishlist = findViewById(R.id.txtEmptyWishlist);
        rvWishlist = findViewById(R.id.rvWishlist);

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        setupWishlist();
    }

    private void setupWishlist() {
        if (WishlistManager.getWishlistItems().isEmpty()) {
            txtEmptyWishlist.setVisibility(View.VISIBLE);
            rvWishlist.setVisibility(View.GONE);
        } else {
            txtEmptyWishlist.setVisibility(View.GONE);
            rvWishlist.setVisibility(View.VISIBLE);
            
            adapter = new WishlistAdapter(this, WishlistManager.getWishlistItems(), this::updateUI);
            rvWishlist.setLayoutManager(new LinearLayoutManager(this));
            rvWishlist.setAdapter(adapter);
        }
    }

    private void updateUI() {
        if (WishlistManager.getWishlistItems().isEmpty()) {
            txtEmptyWishlist.setVisibility(View.VISIBLE);
            rvWishlist.setVisibility(View.GONE);
        }
    }
}
