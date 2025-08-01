package com.iceshardgames.gamercommunity.Activity.LoginScreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.iceshardgames.gamercommunity.Activity.OtpScreen.ForgetScreenActivity;
import com.iceshardgames.gamercommunity.Activity.ProfileScreen.ProfileScreenActivity;
import com.iceshardgames.gamercommunity.Activity.RegisterScreen.RegisterScreenActivity;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.SharedPrefManager;
import com.iceshardgames.gamercommunity.Utills.Utills;
import com.iceshardgames.gamercommunity.databinding.ActivityLoginScreenBinding;

public class
LoginScreenActivity extends AppCompatActivity {

    ActivityLoginScreenBinding binding;

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

        Utills.GradientText(binding.headerStart.screenTitleNav);
        Utills.GradientText(binding.tvVrNexus);
        Clicks();
    }

    private void Clicks() {

        // Set OnClickListener for the "JACK IN" button
        binding.btnJackIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.etEmail.getText().toString().trim();
                String password = binding.etPassword.getText().toString().trim();

                if (email.isEmpty() || password.isEmpty()) {
                    Toast.makeText(LoginScreenActivity.this, "Please enter email and password", Toast.LENGTH_SHORT).show();
                } else {
                    // Perform login logic here
                    Log.d("==login", "Login attempt with Email: " + email + ", Password: " + password);
                    SharedPrefManager.saveEmail(LoginScreenActivity.this, email);
                    Toast.makeText(LoginScreenActivity.this, "Logging in...", Toast.LENGTH_SHORT).show();

                    SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putBoolean("isLoggedIn", true);
                    editor.apply();

                    Intent intent = new Intent(LoginScreenActivity.this, ProfileScreenActivity.class);
                    startActivity(intent);
                    // finish();

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


}