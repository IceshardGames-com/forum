package com.iceshardgames.gamercommunity.Activity.RegisterScreen;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.iceshardgames.gamercommunity.APIintegration.ApiClient;
import com.iceshardgames.gamercommunity.APIintegration.ApiService;
import com.iceshardgames.gamercommunity.Activity.LoginScreen.LoginScreenActivity;
import com.iceshardgames.gamercommunity.Activity.ProfileScreen.ProfileScreenActivity;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.SharedPrefManager;
import com.iceshardgames.gamercommunity.Utills.Utills;
import com.iceshardgames.gamercommunity.databinding.ActivityRegisterScreenBinding;

import java.io.IOException;

import retrofit2.Call;

public class RegisterScreenActivity extends AppCompatActivity {

    ActivityRegisterScreenBinding binding;
    PreferenceManager preferenceManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityRegisterScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Utills.GradientText(binding.headerStart.screenTitleNav);
        binding.headerStart.screenTitleNav.setText("Become a Member");
        Utills.GradientText(binding.tvVrNexus);
        Clicks();

    }

    private void Clicks() {

        // Set OnClickListener for the "SPAWN IN" button
        binding.btnSpawnIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utills.showLoadingDialog(RegisterScreenActivity.this); // Show loading before API call

                String email = binding.etEmail.getText().toString().trim();
                String gamertag = binding.etGamertag.getText().toString().trim();
                String password = binding.etPassword.getText().toString().trim();
                String conpassword = binding.etPassword.getText().toString().trim();

                // 1️⃣ Empty fields check
                if (email.isEmpty()) {
                    binding.etEmail.setError("Email is required");
                    binding.etEmail.requestFocus();
                    return;
                }
                if (gamertag.isEmpty()) {
                    binding.etGamertag.setError("Gamertag is required");
                    binding.etGamertag.requestFocus();
                    return;
                }
                if (password.isEmpty()) {
                    binding.etPassword.setError("Password is required");
                    binding.etPassword.requestFocus();
                    return;
                }

                // 2️⃣ Email format check
                if (!android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.etEmail.setError("Enter a valid email address");
                    binding.etEmail.requestFocus();
                    return;
                }

                // 3️⃣ Gamertag length check
                if (gamertag.length() < 3) {
                    binding.etGamertag.setError("Gamertag must be at least 3 characters");
                    binding.etGamertag.requestFocus();
                    return;
                }

                // 4️⃣ Password strength check
                String passwordPattern = "^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&+=!]).{8,}$";
                if (!password.matches(passwordPattern)) {
                    binding.etPassword.setError("Password must be 8+ chars, include upper, lower, number, and special char");
                    binding.etPassword.requestFocus();
                    return;
                }

                // ✅ If all validations pass → API Call
                Log.e("API_ERROR", email);
                Log.e("API_ERROR", gamertag);
                Log.e("API_ERROR", password);
                RegisterRequest request = new RegisterRequest(gamertag, email, password, password);
                ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
                apiService.registerUser(request).enqueue(new retrofit2.Callback<RegisterResponse>() {
                    @Override
                    public void onResponse(Call<RegisterResponse> call, retrofit2.Response<RegisterResponse> response) {
                        Utills.hideLoadingDialog();
                        binding.textSpawnIn.setText("Registering...");
                        binding.btnSpawnIn.setEnabled(true);

                        if (response.isSuccessful() && response.body() != null) {
                            RegisterResponse registerResponse = response.body();
                            if (registerResponse.isSuccess()) {
                                // Save in preferences
                                // Save in preferences
                                SharedPrefManager preferenceManager = new SharedPrefManager(RegisterScreenActivity.this);
                                preferenceManager.saveUser(gamertag);

                                Toast.makeText(RegisterScreenActivity.this, "Registration Successful", Toast.LENGTH_SHORT).show();
                                startActivity(new Intent(RegisterScreenActivity.this, ProfileScreenActivity.class));
                            } else {
                                Toast.makeText(RegisterScreenActivity.this, registerResponse.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            try {
                                Log.e("API_ERROR", response.errorBody().string());
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            Toast.makeText(RegisterScreenActivity.this,  "User with this email already exists", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<RegisterResponse> call, Throwable t) {
                        Utills.hideLoadingDialog();
                        binding.textSpawnIn.setText("JOIN IN");
                        binding.btnSpawnIn.setEnabled(true);
                        Toast.makeText(RegisterScreenActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Set OnClickListener for "Sign In" text
        binding.tvSignIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("=register", "Navigating to Login screen");
                Intent intent = new Intent(RegisterScreenActivity.this, LoginScreenActivity.class);
                startActivity(intent);

                finish(); // Finish current activity to prevent going back to register on back press
            }
        });

        // Set OnClickListeners for social media buttons (same as LoginActivity)
        binding.btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("=register", "Google login clicked");
                Toast.makeText(RegisterScreenActivity.this, "Google login not implemented", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("=register", "Facebook login clicked");
                Toast.makeText(RegisterScreenActivity.this, "Facebook login not implemented", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnDiscord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("=register", "Discord login clicked");
                Toast.makeText(RegisterScreenActivity.this, "Discord login not implemented", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnSteam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("=register", "Steam login clicked");
                Toast.makeText(RegisterScreenActivity.this, "Steam login not implemented", Toast.LENGTH_SHORT).show();
            }
        });

        // Set OnClickListener for the back button (top nav)
        binding.headerStart.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Go back to the previous activity or close the app
            }
        });

    }

}