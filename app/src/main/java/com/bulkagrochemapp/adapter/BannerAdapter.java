package com.bulkagrochemapp.adapter;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bulkagrochemapp.R;
import com.bulkagrochemapp.model.Slider;
import com.bulkagrochemapp.network.ApiService;
import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {

    private Context context;
    private List<Slider> sliderList;

    public BannerAdapter(Context context, List<Slider> sliderList) {
        this.context = context;
        this.sliderList = sliderList;
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_banner, parent, false);
        return new BannerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Slider slider = sliderList.get(position);
        if (slider != null && slider.getPhoto() != null) {
            String photoName = slider.getPhoto().trim();
            String imageUrl = photoName.startsWith("http") ? photoName : ApiService.BANNER_IMAGE_URL + photoName;

            Log.d("BannerAdapter", "Picasso loading: " + imageUrl);

            Picasso.get()
                    .load(imageUrl)
                    .placeholder(R.drawable.banner_placeholder)
                    .error(R.drawable.ic_image_error)
                    .into(holder.ivBanner, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d("BannerAdapter", "Picasso Success: " + imageUrl);
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("BannerAdapter", "Picasso Error: " + imageUrl + " | " + e.getMessage());
                        }
                    });
        }
    }

    @Override
    public int getItemCount() {
        return sliderList == null ? 0 : sliderList.size();
    }

    public static class BannerViewHolder extends RecyclerView.ViewHolder {
        ImageView ivBanner;
        public BannerViewHolder(@NonNull View itemView) {
            super(itemView);
            ivBanner = itemView.findViewById(R.id.ivBannerItem);
        }
    }
}
