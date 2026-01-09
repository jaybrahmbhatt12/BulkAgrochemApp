package com.bulkagrochemapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.adapter.CartAdapter;
import com.bulkagrochemapp.model.CartItem;
import com.bulkagrochemapp.utils.CartManager;
import com.google.android.material.snackbar.Snackbar;

import androidx.recyclerview.widget.ItemTouchHelper;

import java.util.List;

//=========== Cart Activity ===========//

public class CartActivity extends AppCompatActivity {

    RecyclerView rvCart;
    TextView txtTotal;
    TextView txtEmptyCart;
    ImageButton btnHome;

    //=========== onCreate() ===========//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        rvCart = findViewById(R.id.rvCart);
        txtTotal = findViewById(R.id.txtTotal);
        txtEmptyCart = findViewById(R.id.txtEmptyCart);
        btnHome = findViewById(R.id.btnHome);

        rvCart.setLayoutManager(new LinearLayoutManager(this));

        CartAdapter adapter = new CartAdapter(
                this,
                CartManager.getCartItems(),
                () -> {
                    txtTotal.setText("₹" + CartManager.getTotalAmount());
                    updateCartUI();
                }
        );

        rvCart.setAdapter(adapter);
        txtTotal.setText("₹" + CartManager.getTotalAmount());

        // Redirect to Home Screen with smooth slide transition
        if (btnHome != null) {
            btnHome.setOnClickListener(v -> {
                Intent intent = new Intent(CartActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
                startActivity(intent);
                // Standard slide transition: Enter from left, exit to right
                overridePendingTransition(R.anim.slide_in_left, R.anim.slide_out_right);
                finish();
            });
        }

        ItemTouchHelper.SimpleCallback swipeCallback =
                new ItemTouchHelper.SimpleCallback(0,
                        ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

                    @Override
                    public boolean onMove(
                            RecyclerView recyclerView,
                            RecyclerView.ViewHolder viewHolder,
                            RecyclerView.ViewHolder target) {
                        return false;
                    }

                    @Override
                    public void onSwiped(
                            RecyclerView.ViewHolder viewHolder,
                            int direction) {

                        int position = viewHolder.getAdapterPosition();
                        List<CartItem> items = CartManager.getCartItems();
                        CartItem deletedItem = items.get(position);
                        int deletedPosition = position;

                        // Remove item
                        items.remove(position);
                        rvCart.getAdapter().notifyItemRemoved(position);
                        txtTotal.setText("₹" + CartManager.getTotalAmount());
                        updateCartUI();

                        // Show Snackbar for Undo
                        Snackbar.make(rvCart, "Item removed from cart", 5000)
                                .setAction("UNDO", v -> {
                                    items.add(deletedPosition, deletedItem);
                                    rvCart.getAdapter().notifyItemInserted(deletedPosition);
                                    txtTotal.setText("₹" + CartManager.getTotalAmount());
                                    updateCartUI();
                                }).show();
                    }
                };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvCart);

        updateCartUI();

    }
    private void updateCartUI() {
        if (txtEmptyCart == null) return;

        if (CartManager.getCartItems().isEmpty()) {
            txtEmptyCart.setVisibility(View.VISIBLE);
            rvCart.setVisibility(View.GONE);
            txtTotal.setText("₹0");
        } else {
            txtEmptyCart.setVisibility(View.GONE);
            rvCart.setVisibility(View.VISIBLE);
            txtTotal.setText("₹" + CartManager.getTotalAmount());
        }
    }
}
