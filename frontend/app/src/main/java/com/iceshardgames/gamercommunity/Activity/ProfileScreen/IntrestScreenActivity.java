package com.iceshardgames.gamercommunity.Activity.ProfileScreen;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.iceshardgames.gamercommunity.Activity.MainScreen.DashboardScreenActivity;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;
import com.iceshardgames.gamercommunity.databinding.ActivityIntrestScreenBinding;

import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IntrestScreenActivity extends AppCompatActivity {

    ActivityIntrestScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityIntrestScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        Utills.GradientText(binding.headerStart.screenTitleNav);
        binding.headerStart.screenTitleNav.setText("Forget \nPassword");
        Utills.GradientText(binding.tvIntrest);
        Clicks();

    }

    private void Clicks() {
        if (binding.interestContainer == null) {
            Toast.makeText(this, "interest_container is NULL", Toast.LENGTH_SHORT).show();
            return;
        }
// Clear any previous views to avoid duplicates
        binding.interestContainer.removeAllViews();
        Map<String, List<String>> interestsMap = new LinkedHashMap<>();
        interestsMap.put("🎮 Gaming", Arrays.asList("VR Games", "Mobile Games", "Console Games", "PC Games", "Indie Games"));
        interestsMap.put("📦 Collectibles & Hobbies", Arrays.asList("Toys", "Model Kits", "Stamps", "Coins", "Other"));
        interestsMap.put("💀 Spooky", Arrays.asList("Ghosts", "UFOs", "Myths", "Creepy Stories"));
        interestsMap.put("🌿 Nature & Outdoors", Arrays.asList("Hiking", "Fishing", "Wildlife", "Camping"));
        interestsMap.put("🏡 Home & Garden", Arrays.asList("Gardening", "DIY", "Farming", "Interior Design"));
        interestsMap.put("💬 Communities", Arrays.asList("Reddit", "Discord", "Forums", "Game Dev Meetups"));
        interestsMap.put("🎨 Creativity", Arrays.asList("Game Design", "Cosplay", "Fan Art", "Machinima"));
        interestsMap.put("📺 Streaming", Arrays.asList("Twitch", "YouTube", "Kick", "Gameplay Videos"));

        for (Map.Entry<String, List<String>> entry : interestsMap.entrySet()) {
            addCategory(entry.getKey(), entry.getValue());
        }

        // Add continue button at the end
        ViewGroup parent = (ViewGroup) binding.btnContinue.getParent();
        if (parent != null) {
            parent.removeView(binding.btnContinue);
        }
        binding.interestContainer.addView(binding.btnContinue);
        binding.btnContinue.setOnClickListener(v -> {
            Toast.makeText(this, "Continue", Toast.LENGTH_SHORT).show();
            startActivity(new Intent(this, DashboardScreenActivity.class));
        });

        binding.headerStart.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

    private void addCategory(String title, List<String> chips) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View categoryView = inflater.inflate(R.layout.item_category, binding.interestContainer, false);

        TextView titleText = categoryView.findViewById(R.id.category_title);
        ChipGroup chipGroup = categoryView.findViewById(R.id.chip_group);

        titleText.setText(title);

        for (String chipText : chips) {
            Chip chip = new Chip(this);
            chip.setText(chipText);
            chip.setCheckable(true);
            chip.setTextColor(Color.WHITE);
            chip.setChipBackgroundColorResource(R.color.black);
            chip.setChipStrokeWidth(2);
            chip.setChipStrokeColorResource(android.R.color.transparent);
            chipGroup.addView(chip); // Each chip is a new instance!
        }

        binding.interestContainer.addView(categoryView);
    }
}