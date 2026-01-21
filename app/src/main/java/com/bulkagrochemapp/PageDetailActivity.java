package com.bulkagrochemapp;

import android.os.Bundle;
import android.text.Html;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PageDetailActivity extends AppCompatActivity {

    private TextView txtContent;
    private ProgressBar progressBar;
    private MaterialToolbar toolbar;
    private String slug, title;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_detail);

        slug = getIntent().getStringExtra("slug");
        title = getIntent().getStringExtra("title");

        toolbar = findViewById(R.id.toolbar);
        txtContent = findViewById(R.id.txtContent);
        progressBar = findViewById(R.id.progressBar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(title);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        fetchPageData();
    }

    private void fetchPageData() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.getPageContent(slug).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        if (jsonObject.has("data")) {
                            JSONObject data = jsonObject.getJSONObject("data");
                            String content = data.optString("content");
                            txtContent.setText(Html.fromHtml(content, Html.FROM_HTML_MODE_COMPACT));
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(PageDetailActivity.this, "Error parsing content", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(PageDetailActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
