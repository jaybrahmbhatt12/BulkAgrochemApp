package com.bulkagrochemapp;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.bulkagrochemapp.model.District;
import com.bulkagrochemapp.model.LocationResponse;
import com.bulkagrochemapp.model.State;
import com.bulkagrochemapp.network.ApiService;
import com.bulkagrochemapp.network.RetrofitClient;
import com.google.android.material.appbar.MaterialToolbar;
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

public class ChangeAddressActivity extends AppCompatActivity {

    private TextInputEditText etName, etPhone, etZip, etAddress;
    private AutoCompleteTextView actvState, actvDistrict;
    private Button btnUpdateAddress;
    private MaterialToolbar toolbar;

    private List<State> indianStates = new ArrayList<>();
    private String currentEmail = ""; // To be fetched from prefs

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_change_address);

        initViews();
        setupToolbar();
        fetchLocationData();
        
        btnUpdateAddress.setOnClickListener(v -> updateAddress());
    }

    private void initViews() {
        toolbar = findViewById(R.id.toolbar);
        etName = findViewById(R.id.etName);
        etPhone = findViewById(R.id.etPhone);
        actvState = findViewById(R.id.actvState);
        actvDistrict = findViewById(R.id.actvDistrict);
        etZip = findViewById(R.id.etZip);
        etAddress = findViewById(R.id.etAddress);
        btnUpdateAddress = findViewById(R.id.btnUpdateAddress);
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
                Log.e("ChangeAddress", "Fetch Error: " + t.getMessage());
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
        etName.setText(prefs.getString("name", ""));
        etPhone.setText(prefs.getString("phone", ""));
        currentEmail = prefs.getString("email", "");
        
        String savedState = prefs.getString("state", "");
        if (!savedState.isEmpty()) {
            actvState.setText(savedState, false);
            for (State s : indianStates) {
                if (s.getName().equalsIgnoreCase(savedState)) {
                    updateDistrictSpinner(s.getDistricts());
                    break;
                }
            }
        }
        
        actvDistrict.setText(prefs.getString("district", ""), false);
        etZip.setText(prefs.getString("zip", ""));
        etAddress.setText(prefs.getString("address", ""));
    }

    private void updateAddress() {
        SharedPreferences prefs = getSharedPreferences("user_session", MODE_PRIVATE);
        String userId = prefs.getString("id", "");
        
        if (userId.isEmpty()) {
            Toast.makeText(this, "User ID not found.", Toast.LENGTH_SHORT).show();
            return;
        }

        String name = etName.getText().toString().trim();
        String phone = etPhone.getText().toString().trim();
        String state = actvState.getText().toString().trim();
        String city = actvDistrict.getText().toString().trim();
        String zip = etZip.getText().toString().trim();
        String address = etAddress.getText().toString().trim();

        if (zip.length() != 6) {
            etZip.setError("ZIP Code must be 6 digits");
            return;
        }

        ApiService apiService = RetrofitClient.getClient().create(ApiService.class);
        apiService.updateProfile(userId, name, currentEmail, phone, "India", state, city, zip, address).enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if (response.isSuccessful() && response.body() != null) {
                    try {
                        String jsonString = response.body().string();
                        JSONObject jsonObject = new JSONObject(jsonString);
                        if ("success".equalsIgnoreCase(jsonObject.optString("status"))) {
                            SharedPreferences.Editor editor = prefs.edit();
                            editor.putString("state", state);
                            editor.putString("district", city);
                            editor.putString("zip", zip);
                            editor.putString("address", address);
                            editor.apply();

                            Toast.makeText(ChangeAddressActivity.this, "Address Updated Successfully!", Toast.LENGTH_SHORT).show();
                            setResult(RESULT_OK);
                            finish();
                        } else {
                            Toast.makeText(ChangeAddressActivity.this, jsonObject.optString("message"), Toast.LENGTH_SHORT).show();
                        }
                    } catch (IOException | JSONException e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Toast.makeText(ChangeAddressActivity.this, "Network Error", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
