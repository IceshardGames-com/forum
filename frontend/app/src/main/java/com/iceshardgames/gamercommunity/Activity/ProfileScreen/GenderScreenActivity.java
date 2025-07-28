package com.iceshardgames.gamercommunity.Activity.ProfileScreen;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;
import com.iceshardgames.gamercommunity.databinding.ActivityGenderScreenBinding;

public class GenderScreenActivity extends AppCompatActivity {

    ActivityGenderScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityGenderScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Utills.GradientText(binding.headerStart.screenTitleNav);
        binding.headerStart.screenTitleNav.setText("About You");
        Utills.GradientText(binding.tvAbout);
        setSelectedButton(null);

        Clicks();
    }

    private void Clicks() {
        binding.btnMan.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedButton(binding.btnMan);
               /* if (binding.btnMan.getBackground().getConstantState().equals(
                        ResourcesCompat.getDrawable(getResources(), R.drawable.button_neon_background, null).getConstantState())) {
                    // Button is selected, so deselect it (set to default background)
                    binding.btnMan.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_blur_border, null));
                } else {
                    // Button is not selected, so select it
                    binding.btnMan.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_neon_background, null));
                }*/
            }
        });

        binding.btnWoman.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedButton(binding.btnWoman);
            }
        });

        binding.btnNonbinary.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedButton(binding.btnNonbinary);
            }
        });
        binding.btnPreferNot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setSelectedButton(binding.btnPreferNot);
            }
        });

        binding.btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(GenderScreenActivity.this, IdentifyScreenActivity.class));
            }
        });

        binding.headerStart.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed(); // Go back to the previous activity or close the app
            }
        });
    }

    private void setSelectedButton(Button selectedButton) {
        // List of all gender buttons
        Button[] genderButtons = {
                binding.btnMan,
                binding.btnWoman,
                binding.btnNonbinary,
                binding.btnPreferNot
        };

        // Loop through all buttons
        for (Button button : genderButtons) {
            if (button == selectedButton) {
                // Set selected background for the clicked button
                button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_neon_background, null));
            } else {
                // Set default background for all other buttons
                button.setBackground(ResourcesCompat.getDrawable(getResources(), R.drawable.button_blur_border, null));
            }
        }
    }
}