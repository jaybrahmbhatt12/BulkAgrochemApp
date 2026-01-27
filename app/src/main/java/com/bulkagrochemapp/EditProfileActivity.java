package com.bulkagrochemapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bulkagrochemapp.model.District;
import com.bulkagrochemapp.model.LocationResponse;
import com.bulkagrochemapp.model.State;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.textfield.TextInputEditText;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class EditProfileActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone, etEmail, etZip, etAddress, etCountry;
    private AutoCompleteTextView actvState, actvDistrict;
    private ImageView ivProfileImage;
    private FloatingActionButton fabEditPhoto;
    private Button btnUpdateProfile;
    private MaterialToolbar toolbar;

    private List<State> indianStates = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        initViews();
        setupToolbar();
        fetchLocationData();
        
        btnUpdateProfile.setOnClickListener(v -> updateProfile());
        
        if (fabEditPhoto != null) {
            fabEditPhoto.setOnClickListener(v -> {
                Toast.makeText(this, "Select Image", Toast.LENGTH_SHORT).show();
            });
        }

        if (getIntent().getBooleanExtra("is_first_time", false)) {
            Toast.makeText(this, "Please complete your profile to continue", Toast.LENGTH_LONG).show();
        }
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        ivProfileImage = findViewById(R.id.ivProfileImage);
        fabEditPhoto = findViewById(R.id.fabEditPhoto);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        etEmail = findViewById(R.id.etEmail);
        etCountry = findViewById(R.id.etCountry);
        actvState = findViewById(R.id.actvState);
        actvDistrict = findViewById(R.id.actvDistrict);
        etZip = findViewById(R.id.etZip);
        etAddress = findViewById(R.id.etAddress);
        btnUpdateProfile = findViewById(R.id.btnUpdateProfile);
        
        if (etCountry != null) {
            etCountry.setText("India");
            etCountry.setEnabled(false);
        }
        
        if (etPhone != null) {
            etPhone.setEnabled(false);
            etPhone.setFocusable(false);
        }
    }

    private void setupToolbar() {
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        toolbar.setNavigationOnClickListener(v -> onBackPressed());
    }

    private void fetchLocationData() {
        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);

        apiService.getIndiaLocations().enqueue(new Callback<LocationResponse>() {
            @Override
            public void onResponse(Call<LocationResponse> call, Response<LocationResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    indianStates = response.body().getStates();
                    setupStateSpinner();
                    loadUserData();
                }
            }

            @Override
            public void onFailure(Call<LocationResponse> call, Throwable t) {
                Log.e("EditProfile", "Fetch Error: " + t.getMessage());
            }
        });
    }

    private void setupStateSpinner() {
        List<String> stateNames = new ArrayList<>();
        for (State s : indianStates) {
            stateNames.add(s.getName());
        }

        ArrayAdapter<String> stateAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, stateNames);
        actvState.setAdapter(stateAdapter);

        actvState.setOnItemClickListener((parent, view, position, id) -> {
            String stateName = (String) parent.getItemAtPosition(position);
            for (State s : indianStates) {
                if (s.getName().equals(stateName)) {
                    updateDistrictSpinner(s.getDistricts());
                    break;
                }
            }
        });
    }

    private void updateDistrictSpinner(List<District> districts) {
        List<String> districtNames = new ArrayList<>();
        for (District d : districts) {
            districtNames.add(d.getName());
        }

        ArrayAdapter<String> districtAdapter = new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, districtNames);
        actvDistrict.setAdapter(districtAdapter);
        actvDistrict.setText("", false);
    }

    private void loadUserData() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        
        // Helper method to sanitize strings and remove "null" or default values
        String name = sanitize(prefs.getString("name", ""));
        if (name.equalsIgnoreCase("New User")) name = "";
        etName.setText(name);

        etPhone.setText(sanitize(prefs.getString("phone", "")));

        String email = sanitize(prefs.getString("email", ""));
        if (email.equalsIgnoreCase("user@example.com")) email = "";
        etEmail.setText(email);

        String savedState = sanitize(prefs.getString("state", ""));
        if (!savedState.isEmpty()) {
            actvState.setText(savedState, false);
            for (State s : indianStates) {
                if (s.getName().equalsIgnoreCase(savedState)) {
                    updateDistrictSpinner(s.getDistricts());
                    break;
                }
            }
        } else {
            actvState.setText("", false);
        }

        // Standardize key to "city" to match LoginActivity and server response
        String city = sanitize(prefs.getString("city", ""));
        if (city.isEmpty()) city = sanitize(prefs.getString("district", ""));
        actvDistrict.setText(city, false);

        etZip.setText(sanitize(prefs.getString("zip", "")));
        etAddress.setText(sanitize(prefs.getString("address", "")));
    }

    private String sanitize(String input) {
        if (input == null || input.trim().equalsIgnoreCase("null") || input.isEmpty()) {
            return "";
        }
        return input.trim();
    }

    private void updateProfile() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String userId = prefs.getString("id", "");
        
        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String email = etEmail.getText().toString().trim();
        String country = "India";
        String state = actvState.getText().toString().trim();
        String city = actvDistrict.getText().toString().trim();
        String zip = etZip.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (name.isEmpty() || email.isEmpty()) {
            Toast.makeText(this, "Name and Email are required", Toast.LENGTH_SHORT).show();
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.updateProfile(userId, name, email, phone, country, state, city, zip, address).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonString);
                        if ("success".equalsIgnoreCase(jsonObject.optString("status"))) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("name", name);
                            editor.putString("email", email);
                            editor.putString("state", state);
                            editor.putString("city", city); // Consistent with LoginActivity
                            editor.putString("district", city); // Keep district for compatibility if needed elsewhere
                            editor.putString("zip", zip);
                            editor.putString("address", address);
                            editor.apply();

                            Toast.makeText(EditProfileActivity.this, "Profile Updated!", Toast.LENGTH_SHORT).show();
                            
                            if (getIntent().getBooleanExtra("is_first_time", false)) {
                                startActivity(new Intent(EditProfileActivity.this, MainActivity.class));
                            }
                            finish();
                        }
                    } catch (Exception e) { e.printStackTrace(); }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(EditProfileActivity.this, "Update failed", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
