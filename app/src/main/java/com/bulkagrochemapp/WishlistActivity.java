package com.bulkagrochemapp;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.bulkagrochemapp.adapter.WishlistAdapter;
import com.bulkagrochemapp.utils.WishlistManager;

public class WishlistActivity extends BaseActivity {

    private ImageButton btnBack;
    private TextView txtEmptyWishlist;
    private RecyclerView rvWishlist;
    private ProgressBar progressBarWishlist;
    private WishlistAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wishlist);

        btnBack = findViewById(R.id.btnBack);
        txtEmptyWishlist = findViewById(R.id.txtEmptyWishlist);
        rvWishlist = findViewById(R.id.rvWishlist);
        progressBarWishlist = findViewById(R.id.progressBarWishlist);

        btnBack.setOnClickListener(v -> finish());
    }

    @Override
    protected void onResume() {
        super.onResume();
        loadWishlist();
    }

    private void loadWishlist() {
        // Show progress bar while fetching from remote
        if (progressBarWishlist != null) progressBarWishlist.setVisibility(View.VISIBLE);
        
        WishlistManager.fetchRemoteWishlist(this, () -> {
            runOnUiThread(() -> {
                if (progressBarWishlist != null) progressBarWishlist.setVisibility(View.GONE);
                setupWishlistUI();
            });
        });
    }

    private void setupWishlistUI() {
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
