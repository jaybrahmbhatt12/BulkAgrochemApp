package com.bulkagrochemapp;

import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.adapter.CartAdapter;
import com.bulkagrochemapp.utils.CartManager;

import androidx.recyclerview.widget.ItemTouchHelper;

//=========== Cart Activity ===========//

public class CartActivity extends AppCompatActivity {

    RecyclerView rvCart;
    TextView txtTotal;
    TextView txtEmptyCart;

    //=========== onCreate() ===========//

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_cart);

        rvCart = findViewById(R.id.rvCart);
        txtTotal = findViewById(R.id.txtTotal);

        rvCart.setLayoutManager(new LinearLayoutManager(this));

        CartAdapter adapter = new CartAdapter(
                this,
                CartManager.getCartItems(),
                () -> txtTotal.setText("₹" + CartManager.getTotalAmount())
        );

        rvCart.setAdapter(adapter);
        txtTotal.setText("₹" + CartManager.getTotalAmount());

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

                        CartManager.getCartItems().remove(position);
                        rvCart.getAdapter().notifyItemRemoved(position);

                        // update total
                        txtTotal.setText("₹" + CartManager.getTotalAmount());
                    }
                };

        new ItemTouchHelper(swipeCallback).attachToRecyclerView(rvCart);

        updateCartUI();

    }
    private void updateCartUI() {

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
