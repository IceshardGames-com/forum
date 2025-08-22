package com.iceshardgames.gamercommunity.Activity.OtpScreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.iceshardgames.gamercommunity.APIintegration.ApiClient;
import com.iceshardgames.gamercommunity.APIintegration.ApiService;
import com.iceshardgames.gamercommunity.Activity.LoginScreen.LoginScreenActivity;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;
import com.iceshardgames.gamercommunity.databinding.ActivityResetPasswordScreenBinding;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResetPasswordScreenActivity extends AppCompatActivity {

    private String email;
    ActivityResetPasswordScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityResetPasswordScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Utills.GradientText(binding.headerStart.screenTitleNav);
        binding.headerStart.screenTitleNav.setText("Recover Your Password");
        Utills.GradientText(binding.tvChoosePassword);
        clicks();
    }

    private void clicks() {
        // Get email from intent
        email = getIntent().getStringExtra("email");
        // Password strength listener
        binding.passwordEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                updatePasswordStrength(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Set click listener
        binding.btnresetpass.setOnClickListener(v -> {
            String currentPassword = binding.passwordEditTextold.getText().toString().trim();
            String password = binding.passwordEditText.getText().toString().trim();
            String confirmPassword = binding.confirmPasswordEditText.getText().toString().trim();

            if (validatePasswords(currentPassword, password, confirmPassword)) {
                resetPassword(currentPassword, password);
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

    private void updatePasswordStrength(String password) {
        if (TextUtils.isEmpty(password)) {
            binding.passwordStrengthText.setText("");
            binding.passwordStrengthBar.setProgress(0);
            return;
        }

        int strength = calculatePasswordStrength(password);
        binding.passwordStrengthBar.setProgress(strength);

        if (strength < 40) {
            binding.passwordStrengthText.setText("Weak");
            binding.passwordStrengthText.setTextColor(ContextCompat.getColor(this, R.color.darkgred));
        } else if (strength < 70) {
            binding.passwordStrengthText.setText("Medium");
            binding.passwordStrengthText.setTextColor(ContextCompat.getColor(this, R.color.darkgpink));
        } else {
            binding.passwordStrengthText.setText("Strong");
            binding.passwordStrengthText.setTextColor(ContextCompat.getColor(this, R.color.darkgpurple));
        }
    }

    private int calculatePasswordStrength(String password) {
        int strength = 0;

        // Length
        if (password.length() >= 8) strength += 20;
        if (password.length() >= 12) strength += 10;

        // Contains uppercase
        if (password.matches(".*[A-Z].*")) strength += 15;

        // Contains lowercase
        if (password.matches(".*[a-z].*")) strength += 15;

        // Contains digit
        if (password.matches(".*\\d.*")) strength += 15;

        // Contains special char
        if (password.matches(".*[!@#$%^&*()].*")) strength += 25;

        return Math.min(strength, 100);
    }

    private boolean validatePasswords(String currentPassword, String password, String confirmPassword) {
        if (currentPassword.isEmpty()) {
            binding.passwordEditTextold.setError("Old password cannot be empty");
            return false;
        }
        if (password.isEmpty()) {
            binding.passwordEditText.setError("Password cannot be empty");
            return false;
        } else if (password.length() < 8) {
            binding.passwordEditText.setError("Password must be at least 8 characters");
            return false;
        }
        if (!password.equals(confirmPassword)) {
            binding.confirmPasswordEditText.setError("Passwords don't match");
            return false;
        }
        return true;
    }

    private void resetPassword(String currentPassword, String newPassword) {
        Utills.showLoadingDialog(ResetPasswordScreenActivity.this);
        Log.e("==pass", "currentPassword: " + currentPassword);
        Log.e("==pass", "newPassword: " + newPassword);
        Log.e("==pass", "newPassword: " + newPassword);
        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);
        Log.e("==pass", "token : " + accessToken);

        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        ChangePasswordRequest request = new ChangePasswordRequest(
                currentPassword,   // ✅ old password from user input
                newPassword,       // ✅ new password from user input
                newPassword        // ✅ confirm same as new password
        );
        apiService.changePassword("Bearer " + accessToken, request)
                .enqueue(new Callback<ChangePasswordResponse>() {
                    @Override
                    public void onResponse(Call<ChangePasswordResponse> call, Response<ChangePasswordResponse> response) {
                        Utills.hideLoadingDialog();
                        Log.e("==pass", " " + (response.isSuccessful() && response.body() != null));
                        Log.e("==pass", "response: " + response.isSuccessful());
                        Log.e("==pass", "body: " + response.body());
                        if (response.isSuccessful()) {
                            ChangePasswordResponse res = response.body();
                            if (res.isSuccess()) {
                                Toast.makeText(ResetPasswordScreenActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();

                                // ✅ Update stored password to new one
                                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                                prefs.edit().putString("password", newPassword).apply();

                                startActivity(new Intent(ResetPasswordScreenActivity.this, LoginScreenActivity.class));
                                finishAffinity();
                            } else {
                                Toast.makeText(ResetPasswordScreenActivity.this, res.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Toast.makeText(ResetPasswordScreenActivity.this, "Error code: " + response.code(), Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<ChangePasswordResponse> call, Throwable t) {
                        Utills.hideLoadingDialog();
                        Toast.makeText(ResetPasswordScreenActivity.this, "Failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
}