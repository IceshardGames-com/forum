package com.iceshardgames.gamercommunity.Activity.ProfileScreen;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;
import com.iceshardgames.gamercommunity.databinding.ActivityIdentifyScreenBinding;

public class IdentifyScreenActivity extends AppCompatActivity {

    ActivityIdentifyScreenBinding binding;
    private Typeface customFont; // Declare Typeface as a class variable

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityIdentifyScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        customFont = ResourcesCompat.getFont(this, R.font.noto_italic);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Utills.GradientText(binding.headerStart.screenTitleNav);
        binding.headerStart.screenTitleNav.setText("Identify \\nYourself");
        Utills.GradientText(binding.tvIdentify);
        Clicks();

    }

    private void Clicks() {
        String[] usageReasons = {"Connect with gamers", "Give feedback", "Get game updates", "Community support"};
        String[] industries = {"Student", "Game Developer", "Streamer", "VR Enthusiast", "Other"};

       /* ArrayAdapter<String> usageAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, usageReasons);
        binding.spinnerUsage.setAdapter(usageAdapter);*/


        // Create adapter with custom font
        ArrayAdapter<String> usageAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                usageReasons) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                applyFontAndColor(view);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                applyFontAndColor(view);
                return view;
            }

            private void applyFontAndColor(View view) {
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTypeface(customFont);
                text.setTextColor(Color.WHITE); // Set your text color here
                text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12); // Set font size to 16sp

                // You can add other styling here if needed
            }
        };

        ArrayAdapter<String> industryAdapter = new ArrayAdapter<String>(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                industries) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                applyFontAndColor(view);
                return view;
            }

            @Override
            public View getDropDownView(int position, View convertView, ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                applyFontAndColor(view);
                return view;
            }

            private void applyFontAndColor(View view) {
                TextView text = (TextView) view.findViewById(android.R.id.text1);
                text.setTypeface(customFont);
                text.setTextColor(Color.WHITE); // Set your text color here
                text.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12); // Set font size to 16sp

            }
        };

        binding.spinnerUsage.setAdapter(usageAdapter);
        binding.spinnerIndustry.setAdapter(industryAdapter);

        binding.btnContinue.setOnClickListener(v -> {
            String selectedUsage = binding.spinnerUsage.getSelectedItem().toString();
            String selectedIndustry = binding.spinnerIndustry.getSelectedItem().toString();
            startActivity(new Intent(IdentifyScreenActivity.this, IntrestScreenActivity.class));

            Toast.makeText(this, "Saved: " + selectedUsage + ", " + selectedIndustry, Toast.LENGTH_SHORT).show();
            // Save to DB or SharedPreferences or continue to next screen
        });

        binding.headerStart.backButton.setOnClickListener(
                v -> onBackPressed()
        );

    }
}