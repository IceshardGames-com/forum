package com.iceshardgames.gamercommunity.Activity.OtpScreen;

import static android.view.View.VISIBLE;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.iceshardgames.gamercommunity.Activity.LoginScreen.LoginScreenActivity;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;
import com.iceshardgames.gamercommunity.databinding.ActivityForgetScreenBinding;

public class ForgetScreenActivity extends AppCompatActivity {

    ActivityForgetScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityForgetScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Utills.GradientText(binding.headerStart.screenTitleNav);
        binding.headerStart.screenTitleNav.setText("Forget \nPassword");
        Utills.GradientText(binding.tvForgetPassword);
        Clicks();
    }

    private void Clicks() {

        binding.etEmail.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String email = binding.etEmail.getText().toString().trim();
               /* if(!email.isEmpty()){
                    binding.imgClose.setVisibility(VISIBLE);
                }else {
                    binding.imgClose.setVisibility(View.GONE);
                }*/
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });

        binding.imgClose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
            }
        });
        binding.btnSendPassword.setOnClickListener(v -> {
            String email = binding.etEmail.getText().toString().trim();
            if (validateEmail(email)) {
                // Send OTP to email
                sendOtpToEmail(email);
            }
        });

        binding.backToLoginlayout.setOnClickListener(v -> {
            // Navigate back to login
            startActivity(new Intent(this, LoginScreenActivity.class));
            finish();
        });

        // Set OnClickListener for the back button (top nav)
        binding.headerStart.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Go back to the previous activity or close the app
            }
        });
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            binding.etEmail.setError("Email cannot be empty");
            return false;
        } else if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.etEmail.setError("Please enter a valid email");
            return false;
        }
        return true;
    }

    private void sendOtpToEmail(String email) {
        // Show loading
        ProgressDialog progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("Sending OTP...");
        progressDialog.show();

        // Simulate network call
        new Handler().postDelayed(() -> {
            progressDialog.dismiss();

            // Navigate to OTP verification screen
            Intent intent = new Intent(this, OtpVerificationScreenActivity.class);
            intent.putExtra("email", email);
            startActivity(intent);

            // Show success message
            Toast.makeText(this, "OTP sent to your email", Toast.LENGTH_SHORT).show();
        }, 1500);
    }


}