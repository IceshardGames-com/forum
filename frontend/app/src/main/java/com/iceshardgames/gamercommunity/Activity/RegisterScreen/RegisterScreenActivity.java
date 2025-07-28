package com.iceshardgames.gamercommunity.Activity.RegisterScreen;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.iceshardgames.gamercommunity.Activity.LoginScreen.LoginScreenActivity;
import com.iceshardgames.gamercommunity.Activity.ProfileScreen.ProfileScreenActivity;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;
import com.iceshardgames.gamercommunity.databinding.ActivityRegisterScreenBinding;

public class RegisterScreenActivity extends AppCompatActivity {

    ActivityRegisterScreenBinding binding;

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
        binding.headerStart.screenTitleNav.setText("Join Here");
        Utills.GradientText(binding.tvVrNexus);
        Clicks();

    }

    private void Clicks() {

        // Set OnClickListener for the "SPAWN IN" button
        binding.btnSpawnIn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = binding.etEmail.getText().toString().trim();
                String gamertag = binding.etGamertag.getText().toString().trim();
                String password = binding.etPassword.getText().toString().trim();

                if (email.isEmpty() || gamertag.isEmpty() || password.isEmpty()) {
                    Toast.makeText(RegisterScreenActivity.this, "Please fill all fields", Toast.LENGTH_SHORT).show();
                } else {
                    // Perform registration logic here
                    Log.d("=register", "Registration attempt with Email: " + email + ", Gamertag: " + gamertag + ", Password: " + password);
                    Toast.makeText(RegisterScreenActivity.this, "Registering...", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(RegisterScreenActivity.this, ProfileScreenActivity.class);
                    startActivity(intent);// Example: Navigate to Login activity after successful registration
                    // Intent intent = new Intent(RegisterScreenActivity.this, LoginActivity.class);
                    // startActivity(intent);
                    // finish();
                }
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