package com.bulkagrochemapp;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.squareup.picasso.Picasso;

import org.json.JSONArray;
import org.json.JSONObject;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class PageDetailActivity extends AppCompatActivity {

    private WebView webViewContent;
    private ImageView imgPage;
    private ProgressBar progressBar;
    private MaterialToolbar toolbar;
    private String slug, intentTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_page_detail);

        slug = getIntent().getStringExtra("slug");
        intentTitle = getIntent().getStringExtra("title");

        toolbar = findViewById(R.id.toolbar);
        webViewContent = findViewById(R.id.webViewContent);
        imgPage = findViewById(R.id.imgPage);
        progressBar = findViewById(R.id.progressBar);

        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(intentTitle);
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        setupWebView();

        if (slug != null) {
            fetchPageData();
        } else {
            showError("Invalid Page Link");
        }
    }

    private void setupWebView() {
        WebSettings webSettings = webViewContent.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setDefaultFontSize(15);
        webViewContent.setWebViewClient(new WebViewClient());
    }

    private void fetchPageData() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        
        Log.d("PAGE_API", "Requesting Slug: " + slug);

        apiService.getPageContent(slug).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        Log.d("PAGE_API", "Raw JSON: " + jsonResponse);
                        
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        
                        if (jsonObject.optBoolean("status", false)) {
                            JSONObject selectedPage = null;
                            Object dataObj = jsonObject.get("data");

                            if (dataObj instanceof JSONArray) {
                                JSONArray dataArray = (JSONArray) dataObj;
                                for (int i = 0; i < dataArray.length(); i++) {
                                    JSONObject page = dataArray.getJSONObject(i);
                                    if (page.optString("slug").equalsIgnoreCase(slug)) {
                                        selectedPage = page;
                                        break;
                                    }
                                }
                            } else if (dataObj instanceof JSONObject) {
                                selectedPage = (JSONObject) dataObj;
                            }

                            if (selectedPage != null) {
                                renderPage(selectedPage);
                            } else {
                                showError("Page not found in database (Slug: " + slug + ")");
                            }
                        } else {
                            showError(jsonObject.optString("message", "Server reported error"));
                        }
                    } catch (Exception e) {
                        Log.e("PAGE_API", "Error: ", e);
                        showError("Data Error: " + e.getMessage());
                    }
                } else {
                    showError("Server Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Network connection failed");
            }
        });
    }

    private void renderPage(JSONObject page) {
        String details = page.optString("details", "");
        String photo = page.optString("photo", "");
        String dbTitle = page.optString("title", "");

        if (getSupportActionBar() != null && !dbTitle.isEmpty()) {
            getSupportActionBar().setTitle(dbTitle);
        }

        if (!photo.isEmpty() && !photo.equals("null")) {
            imgPage.setVisibility(View.VISIBLE);
            Picasso.get().load(ApiService.BANNER_IMAGE_URL + photo).into(imgPage);
        }

        // Check if visible text is empty
        if (details.replaceAll("<[^>]*>", "").trim().isEmpty()) {
            details = "<div style='text-align:center; margin-top:50px; font-family:sans-serif; color:#666;'>" +
                    "<h3>Coming Soon</h3><p>We are currently updating our " + dbTitle + ". Please check back later.</p></div>";
        }

        String styledHtml = "<html><head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">" +
                "<style>body{font-family: 'sans-serif'; color: #333; line-height: 1.6; padding: 10px;} " +
                "img{max-width: 100% !important; height: auto !important;} " +
                "div, section, table { max-width: 100% !important; width: auto !important; height: auto !important; }</style>" +
                "</head><body>" + details + "</body></html>";
        
        webViewContent.loadDataWithBaseURL(null, styledHtml, "text/html", "UTF-8", null);
    }

    private void showError(String message) {
        String errorHtml = "<html><body><div style='text-align:center; margin-top:50px; font-family:sans-serif; color:red;'>" +
                "<h4>Error</h4><p>" + message + "</p></div></body></html>";
        webViewContent.loadDataWithBaseURL(null, errorHtml, "text/html", "UTF-8", null);
    }
}
