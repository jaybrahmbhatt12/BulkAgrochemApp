package com.bulkagrochemapp.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.R;
import com.bulkagrochemapp.model.Coupon;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CouponAdapter extends RecyclerView.Adapter<CouponAdapter.CouponViewHolder> {

    private Context context;
    private List<Coupon> couponList;
    private OnCouponClickListener listener;

    public interface OnCouponClickListener {
        void onCouponClick(Coupon coupon);
    }

    public CouponAdapter(Context context, List<Coupon> couponList) {
        this.context = context;
        this.couponList = couponList;
    }

    public CouponAdapter(Context context, List<Coupon> couponList, OnCouponClickListener listener) {
        this.context = context;
        this.couponList = couponList;
        this.listener = listener;
    }

    @NonNull
    @Override
    public CouponViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_coupon, parent, false);
        return new CouponViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CouponViewHolder holder, int position) {
        Coupon coupon = couponList.get(position);
        holder.txtCouponCode.setText(coupon.getCode());

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCouponClick(coupon);
            } else {
                // Fallback to copy to clipboard if no listener is set
                copyToClipboard(coupon.getCode());
            }
        });

        holder.txtCouponCode.setOnClickListener(v -> {
            if (listener != null) {
                listener.onCouponClick(coupon);
            } else {
                copyToClipboard(coupon.getCode());
            }
        });

        String discountText;
        // Changes: type 1 = price, type 0 = percentage
        if (coupon.getType() == 1) {
            discountText = String.format(Locale.getDefault(), "₹%.0f OFF", coupon.getPrice());
        } else {
            discountText = String.format(Locale.getDefault(), "%.0f%% OFF", coupon.getPrice());
        }
        holder.txtDiscountValue.setText(discountText);

        // Changes: Show above_price only if it is > 0
        if (coupon.getAbovePrice() > 0) {
            holder.txtCouponDescription.setVisibility(View.VISIBLE);
            holder.txtCouponDescription.setText(String.format(Locale.getDefault(), "Valid on orders above ₹%.0f", coupon.getAbovePrice()));
        } else {
            holder.txtCouponDescription.setVisibility(View.GONE);
        }

        holder.txtCouponExpiry.setText("Valid Upto: " + formatDate(coupon.getEndDate()));
    }

    private void copyToClipboard(String code) {
        ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("Coupon Code", code);
        if (clipboard != null) {
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Coupon code copied to clipboard", Toast.LENGTH_SHORT).show();
        }
    }

    private String formatDate(String dateStr) {
        if (dateStr == null || dateStr.isEmpty()) return "";

        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            Date date = inputFormat.parse(dateStr);
            if (date != null) {
                return outputFormat.format(date);
            }
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return dateStr;
    }

    @Override
    public int getItemCount() {
        return couponList != null ? couponList.size() : 0;
    }

    public static class CouponViewHolder extends RecyclerView.ViewHolder {
        TextView txtCouponCode, txtDiscountValue, txtCouponDescription, txtCouponExpiry;

        public CouponViewHolder(@NonNull View itemView) {
            super(itemView);
            txtCouponCode = itemView.findViewById(R.id.txtCouponCode);
            txtDiscountValue = itemView.findViewById(R.id.txtDiscountValue);
            txtCouponDescription = itemView.findViewById(R.id.txtCouponDescription);
            txtCouponExpiry = itemView.findViewById(R.id.txtCouponExpiry);
        }
    }
}
