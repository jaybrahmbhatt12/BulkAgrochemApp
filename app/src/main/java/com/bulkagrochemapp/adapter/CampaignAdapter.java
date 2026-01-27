package com.bulkagrochemapp.adapter;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.R;
import com.bulkagrochemapp.model.Campaign;

import java.util.List;

public class CampaignAdapter extends RecyclerView.Adapter<CampaignAdapter.ViewHolder> {

    private Context context;
    private List<Campaign> campaignList;

    public CampaignAdapter(Context context, List<Campaign> campaignList) {
        this.context = context;
        this.campaignList = campaignList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_campaign, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Campaign campaign = campaignList.get(position);
        
        holder.txtWelcomeMessage.setText(campaign.getWelcomeMessage());
        holder.txtCampaignName.setText(campaign.getCampaignName());
        holder.txtOfferCode.setText(campaign.getOfferCode());
        
        // Custom styling based on API data
        applyTheme(holder, campaign);

        if (campaign.getOfferDetails() != null && !campaign.getOfferDetails().isEmpty()) {
            Campaign.OfferDetail detail = campaign.getOfferDetails().get(0);
            holder.txtOfferDetail.setText("Above price: " + detail.getKey());
            holder.txtOfferValue.setText(detail.getValue() + "% OFF");
        } else {
            holder.txtOfferValue.setText(campaign.getOfferDescription());
        }

        View.OnClickListener copyListener = v -> {
            ClipboardManager clipboard = (ClipboardManager) context.getSystemService(Context.CLIPBOARD_SERVICE);
            ClipData clip = ClipData.newPlainText("Offer Code", campaign.getOfferCode());
            clipboard.setPrimaryClip(clip);
            Toast.makeText(context, "Code Copied: " + campaign.getOfferCode(), Toast.LENGTH_SHORT).show();
        };

        holder.ivCopyCode.setOnClickListener(copyListener);
        holder.layoutOfferCode.setOnClickListener(copyListener);
    }

    private void applyTheme(ViewHolder holder, Campaign campaign) {
        int color = Color.parseColor("#2E7D32"); // Default Green
        
        if (campaign.getCustomHex() != null && !campaign.getCustomHex().isEmpty()) {
            try {
                color = Color.parseColor(campaign.getCustomHex());
            } catch (Exception ignored) {}
        } else if ("indigo".equalsIgnoreCase(campaign.getTheme())) {
            color = Color.parseColor("#3F51B5");
        } else if ("fountain".equalsIgnoreCase(campaign.getTheme())) {
            color = Color.parseColor("#009688");
        }

        GradientDrawable gd = new GradientDrawable(
                GradientDrawable.Orientation.TOP_BOTTOM,
                new int[] { color, adjustAlpha(color, 0.8f) }
        );
        gd.setCornerRadius(16f);
        holder.campaignRoot.setBackground(gd);
    }

    private int adjustAlpha(int color, float factor) {
        int alpha = Math.round(Color.alpha(color) * factor);
        int red = Color.red(color);
        int green = Color.green(color);
        int blue = Color.blue(color);
        return Color.argb(alpha, red, green, blue);
    }

    @Override
    public int getItemCount() {
        return campaignList != null ? campaignList.size() : 0;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtWelcomeMessage, txtCampaignName, txtOfferValue, txtOfferCode, txtOfferDetail;
        LinearLayout campaignRoot, layoutOfferCode;
        ImageView ivCopyCode;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtWelcomeMessage = itemView.findViewById(R.id.txtWelcomeMessage);
            txtCampaignName = itemView.findViewById(R.id.txtCampaignName);
            txtOfferValue = itemView.findViewById(R.id.txtOfferValue);
            txtOfferCode = itemView.findViewById(R.id.txtOfferCode);
            txtOfferDetail = itemView.findViewById(R.id.txtOfferDetail);
            campaignRoot = itemView.findViewById(R.id.campaignRoot);
            layoutOfferCode = itemView.findViewById(R.id.layoutOfferCode);
            ivCopyCode = itemView.findViewById(R.id.ivCopyCode);
        }
    }
}
