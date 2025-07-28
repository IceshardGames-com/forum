package com.iceshardgames.gamercommunity.Activity.ProfileScreen;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;
import com.iceshardgames.gamercommunity.databinding.ActivityProfileScreenBinding;

public class ProfileScreenActivity extends AppCompatActivity {

    ActivityProfileScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityProfileScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Utills.GradientText(binding.headerStart.screenTitleNav);
        binding.headerStart.screenTitleNav.setText("Username");
        Utills.GradientText(binding.tvUsername);
        Clicks();
    }

    private void Clicks() {

        binding.passwordStrengthText.setVisibility(TextView.GONE);

        binding.etUsername.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {}

            @Override
            public void afterTextChanged(Editable s) {
                validateUsername(s.toString());
            }
        });

        binding.btnContinue.setOnClickListener(v -> {
            if (validateUsername(binding.etUsername.getText().toString())) {
                showToast("Username is valid!");
                startActivity(new Intent(ProfileScreenActivity.this, GenderScreenActivity.class));

                // Proceed with form submission
            } else {
                showToast("Please correct the username errors.");
            }
        });
        binding.headerStart.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private boolean validateUsername(String username) {
        // Clear previous errors first, regardless of the method chosen
        binding.etUsername.setError(null); // Clears the small error icon/popup
        binding.passwordStrengthText.setVisibility(TextView.GONE); // Hide the error TextView

        username = username.trim();
        String errorMessage = null; // To store the first error found

        // 1. Empty/Required Field Check
        if (username.isEmpty()) {
            errorMessage = "Username cannot be empty";
        }
        // 2. Minimum Length Check
        else if (username.length() < 5) {
            errorMessage = "Username must be at least 5 characters long";
        }
        // 3. Maximum Length Check
        else if (username.length() > 20) {
            errorMessage = "Username cannot exceed 20 characters";
        }
        // 4. Allowed Characters (Alphanumeric, underscores, and dots)
        else if (!username.matches("^[a-zA-Z0-9_.]+$")) {
            errorMessage = "Username can only contain letters, numbers, underscores, and dots";
        }
        // 5. No consecutive special characters (like .. or __)
        else if (username.matches(".*[_.]{2,}.*")) {
            errorMessage = "Username cannot have consecutive special characters (like .. or __)";
        }
        // 6. Prevent special characters at the beginning or end
        else if (username.matches("^[_.].*|.*[_.]$")) {
            errorMessage = "Username cannot start or end with underscore or dot";
        }
        // 7. Reserved Keywords
        else { // Only check reserved keywords if previous checks pass to avoid multiple errors
            String[] reservedKeywords = {"admin", "root", "guest", "test"};
            for (String keyword : reservedKeywords) {
                if (username.equalsIgnoreCase(keyword)) {
                    errorMessage = "This username is reserved";
                    break; // Exit loop once a reserved keyword is found
                }
            }
        }

        // Display the error if one was found
        if (errorMessage != null) {
            // Option A: Use EditText.setError() (shows a small icon and popup on focus)
            binding.etUsername.setError(errorMessage);

            // Option B: Use a separate TextView below the EditText (more similar to TextInputLayout)
            binding.passwordStrengthText.setText(errorMessage);
            binding.passwordStrengthText.setTextColor(Color.RED); // Or a color from your resources
            binding.passwordStrengthText.setVisibility(TextView.VISIBLE);

            return false;
        }

        // If no errors were found, clear any existing error messages
        binding.etUsername.setError(null); // Clear EditText's error icon
        binding.passwordStrengthText.setVisibility(TextView.GONE); // Hide the TextView error
        return true;
    }

    private void showToast(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }
}