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
            getSupportActionBar().setTitle(intentTitle != null ? intentTitle : "Page");
        }
        toolbar.setNavigationOnClickListener(v -> finish());

        setupWebView();

        if (slug != null && !slug.trim().isEmpty()) {
            fetchPageData();
        } else {
            showError("Invalid Page Link: Slug is missing");
        }
    }

    private void setupWebView() {
        WebSettings webSettings = webViewContent.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setLoadWithOverviewMode(true);
        webSettings.setUseWideViewPort(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDefaultFontSize(16);
        webViewContent.setWebViewClient(new WebViewClient());
    }

    private void fetchPageData() {
        progressBar.setVisibility(View.VISIBLE);
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        
        final String searchSlug = slug.trim();
        Log.d("PAGE_API", "Requesting content for slug: [" + searchSlug + "]");

        apiService.getPageContent(searchSlug).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                progressBar.setVisibility(View.GONE);
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonResponse = response.body().string();
                        Log.d("PAGE_API", "Response received, parsing...");
                        
                        JSONObject jsonObject = new JSONObject(jsonResponse);
                        boolean status = jsonObject.optBoolean("status", false);
                        
                        if (status) {
                            JSONObject selectedPage = null;
                            Object data = jsonObject.opt("data");

                            if (data instanceof JSONObject) {
                                selectedPage = (JSONObject) data;
                            } else if (data instanceof JSONArray) {
                                JSONArray array = (JSONArray) data;
                                for (int i = 0; i < array.length(); i++) {
                                    JSONObject item = array.getJSONObject(i);
                                    if (item.optString("slug").equalsIgnoreCase(searchSlug)) {
                                        selectedPage = item;
                                        break;
                                    }
                                }
                            }

                            if (selectedPage != null) {
                                String details = selectedPage.optString("details", "");
                                if (!details.isEmpty() && !details.equalsIgnoreCase("null")) {
                                    renderPage(selectedPage);
                                } else {
                                    showError("The page was found but its 'details' content is missing from the server.");
                                }
                            } else {
                                showError("Page not found in system (Slug: " + searchSlug + ")");
                            }
                        } else {
                            String msg = jsonObject.optString("message", "Error fetching page data");
                            showError(msg);
                        }
                    } catch (Exception e) {
                        Log.e("PAGE_API", "Parsing error", e);
                        showError("Data Error: Unable to process response.");
                    }
                } else {
                    showError("Server Error: " + response.code());
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                showError("Network error: Please check your connection");
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
            Picasso.get().load(ApiService.BANNER_IMAGE_URL + photo)
                    .placeholder(R.drawable.banner_placeholder)
                    .error(R.drawable.ic_image_error)
                    .into(imgPage);
        } else {
            imgPage.setVisibility(View.GONE);
        }

        // AGGRESSIVE CSS RESET: Forces visibility and responsiveness for complex Elementor HTML
        String styledHtml = "<html><head>" +
                "<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\">" +
                "<style>" +
                "body { font-family: 'Poppins', sans-serif, Arial; color: #333; line-height: 1.8; padding: 15px; margin: 0; } " +
                "img { max-width: 100% !important; height: auto !important; border-radius: 8px; margin: 10px 0; } " +
                "h1, h2, h3, h4 { color: #2E7D32; font-weight: 600; margin-top: 15px; } " +
                "p { margin-bottom: 12px; } " +
                "/* Force responsiveness and visibility for Elementor layouts */ " +
                "div, section, article, table, td, h1, h2, h3, p, span, a { " +
                "   max-width: 100% !important; " +
                "   width: auto !important; " +
                "   height: auto !important; " +
                "   position: static !important; " +
                "   float: none !important; " +
                "   margin-left: 0 !important; " +
                "   margin-right: 0 !important; " +
                "   left: 0 !important; " +
                "   top: 0 !important; " +
                "   overflow-wrap: break-word !important; " +
                "   box-sizing: border-box !important; " +
                "   visibility: visible !important; " +
                "   opacity: 1 !important; " +
                "} " +
                ".elementor-widget-container { padding: 0 !important; } " +
                "</style></head><body>" + details + "</body></html>";
        
        // Critical: Set BANNER_IMAGE_URL as baseUrl so relative image paths in the HTML can resolve
        webViewContent.loadDataWithBaseURL(ApiService.BANNER_IMAGE_URL, styledHtml, "text/html", "UTF-8", null);
    }

    private void showError(String message) {
        String errorHtml = "<html><body><div style='text-align:center; margin-top:80px; font-family:sans-serif; color:#D32F2F;'>" +
                "<h3 style='margin-bottom:10px;'>Error</h3><p>" + message + "</p>" +
                "<button onclick='location.reload()' style='background:#2E7D32; color:white; border:none; padding:10px 20px; border-radius:4px; margin-top:20px;'>Retry</button></div></body></html>";
        webViewContent.loadDataWithBaseURL(null, errorHtml, "text/html", "UTF-8", null);
    }
}
