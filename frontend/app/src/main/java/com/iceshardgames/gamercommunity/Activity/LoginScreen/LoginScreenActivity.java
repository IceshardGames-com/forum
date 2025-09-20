package com.iceshardgames.gamercommunity.Activity.LoginScreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.iceshardgames.gamercommunity.APIintegration.ApiClient;
import com.iceshardgames.gamercommunity.APIintegration.ApiService;
import com.iceshardgames.gamercommunity.Activity.MainScreen.DashboardScreenActivity;
import com.iceshardgames.gamercommunity.Activity.OtpScreen.ForgetScreenActivity;
import com.iceshardgames.gamercommunity.Activity.ProfileScreen.ProfileScreenActivity;
import com.iceshardgames.gamercommunity.Activity.RegisterScreen.RegisterScreenActivity;
import com.iceshardgames.gamercommunity.DB.AppDatabase;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.SessionManager;
import com.iceshardgames.gamercommunity.Utills.SharedPrefManager;
import com.iceshardgames.gamercommunity.Utills.Utills;
import com.iceshardgames.gamercommunity.databinding.ActivityLoginScreenBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class
LoginScreenActivity extends AppCompatActivity {

    ActivityLoginScreenBinding binding;
    ApiService apiService;
    private AppDatabase db;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        binding = ActivityLoginScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.circleGradientImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                startActivity(new Intent(LoginScreenActivity.this,DashboardScreenActivity.class));
            }
        });
        Utills.GradientText(binding.headerStart.screenTitleNav);
        Utills.GradientText(binding.tvVrNexus);
        Clicks();
    }

    private void Clicks() {

        // Set OnClickListener for the "JACK IN" button
        binding.btnJackIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateInputs()) {
                    Utills.showLoadingDialog(LoginScreenActivity.this); // Show loading before API call
                    String email = binding.etEmail.getText().toString().trim();
                    String password = binding.etPassword.getText().toString().trim();
                    loginApi(email, password);
                }else {
                    Toast.makeText(LoginScreenActivity.this, "Invalid Inputs", Toast.LENGTH_SHORT).show();
                }
            }
        });


        // Set OnClickListener for "Create Account" text
        binding.tvCreateAccountlayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("==login", "Navigating to Register screen");
                Intent intent = new Intent(LoginScreenActivity.this, RegisterScreenActivity.class);
                startActivity(intent);
            }
        });

        // Set OnClickListener for "Create Account" text
        binding.tvForgotPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginScreenActivity.this, ForgetScreenActivity.class);
                startActivity(intent);
            }
        });

        // Set OnClickListeners for social media buttons
        binding.btnGoogle.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("==login", "Google login clicked");
                Toast.makeText(LoginScreenActivity.this, "Google login not implemented", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnFacebook.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("==login", "Facebook login clicked");
                Toast.makeText(LoginScreenActivity.this, "Facebook login not implemented", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnDiscord.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("==login", "Discord login clicked");
                Toast.makeText(LoginScreenActivity.this, "Discord login not implemented", Toast.LENGTH_SHORT).show();
            }
        });

        binding.btnSteam.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("==login", "Steam login clicked");
                Toast.makeText(LoginScreenActivity.this, "Steam login not implemented", Toast.LENGTH_SHORT).show();
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

    private boolean validateInputs() {
        String email = binding.etEmail.getText().toString().trim();
        String password = binding.etPassword.getText().toString().trim();

        boolean isValid = true;

        // Validate Email
        if (email.isEmpty()) {
            binding.etEmail.setError("Email is required");
            binding.etEmail.requestFocus();
            isValid = false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Enter a valid email");
            binding.etEmail.requestFocus();
            isValid = false;
        }

        // Validate Password
        if (password.isEmpty()) {
            binding.etPassword.setError("Password is required");
            if (isValid) binding.etPassword.requestFocus(); // Focus only if email was valid
            isValid = false;
        } else if (password.length() < 6) { // You can increase to 8 if needed
            binding.etPassword.setError("Password must be at least 6 characters");
            if (isValid) binding.etPassword.requestFocus();
            isValid = false;
        }

        return isValid;
    }

    private void loginApi(String email, String password) {

        LoginRequest request = new LoginRequest(email, password);
        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        apiService.loginUser(request).enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                Utills.hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null) {
                    LoginResponse loginResponse = response.body();

                    if (loginResponse.isSuccess()) {
                        Toast.makeText(LoginScreenActivity.this, "Login Successful", Toast.LENGTH_SHORT).show();

                        // Save tokens and user data
                        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        prefs.edit()
                                .putBoolean("isLoggedIn", true)
                                .putString("refreshToken", loginResponse.getData().getTokens().getRefreshToken())
                                .putString("username", loginResponse.getData().getUser().getUsername())
                                .putString("password", password) // ✅ Store old password here
                                .apply();

                        prefs.edit()
                                .putString("accessToken", loginResponse.getData().getTokens().getAccessToken())
                                .apply();

                        prefs.edit()
                                .putString("userID", loginResponse.getData().getUser().getId())
                                .apply();

                        // ✅ Save userId for DB session
                        SessionManager.saveUserId(LoginScreenActivity.this, loginResponse.getData().getUser().getId());

                        // ✅ Initialize per-user database
                        String currentUserId = SessionManager.getUserId(LoginScreenActivity.this);
                        db = AppDatabase.getInstance(LoginScreenActivity.this, currentUserId);

                        SharedPrefManager preferenceManager = new SharedPrefManager(LoginScreenActivity.this);
                        preferenceManager.saveUser(loginResponse.getData().getUser().getUsername());

                        String userId = loginResponse.getData().getUser().getId();
                        String username = loginResponse.getData().getUser().getUsername();
                        preferenceManager.saveUsernameById(userId, username);
                        Log.d("==saveUser", "Saved username=" + username + " for userId=" + userId);

                        Log.e("==pass", "token: "+loginResponse.getData().getTokens().getAccessToken() );
                        Log.e("==pass", "id: "+loginResponse.getData().getUser().getId() );

                        // after login success
                        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        sp.edit()
                                .putString("accessToken",  loginResponse.getData().getTokens().getAccessToken())
                                .putString("userId",      loginResponse.getData().getUser().getId())
                                .apply();

                        Utills.registerDeviceAtLogin(LoginScreenActivity.this); // use the current activity or getApplicationContext()

                        startActivity(new Intent(LoginScreenActivity.this, ProfileScreenActivity.class));
                        finish();
                    } else {
                        Toast.makeText(LoginScreenActivity.this, loginResponse.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Toast.makeText(LoginScreenActivity.this, "Invalid credentials", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {

                Utills.hideLoadingDialog();
                Toast.makeText(LoginScreenActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.e("LoginAPI", "Failure", t);
            }
        });
    }

}