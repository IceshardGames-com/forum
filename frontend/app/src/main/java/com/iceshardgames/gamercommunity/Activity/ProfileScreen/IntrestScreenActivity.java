package com.iceshardgames.gamercommunity.Activity.ProfileScreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.iceshardgames.gamercommunity.APIintegration.ApiClient;
import com.iceshardgames.gamercommunity.APIintegration.ApiService;
import com.iceshardgames.gamercommunity.Activity.MainScreen.DashboardScreenActivity;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;
import com.iceshardgames.gamercommunity.databinding.ActivityIntrestScreenBinding;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

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
        binding.headerStart.screenTitleNav.setText("Your Interests");
        Utills.GradientText(binding.tvIntrest);
        Utills.showLoadingDialog(IntrestScreenActivity.this);
        loadInterestsFromApi();
    }

    private void loadInterestsFromApi() {
        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        apiService.getInterests(1, 50).enqueue(new Callback<InterestsResponse>() {
            @Override
            public void onResponse(Call<InterestsResponse> call, Response<InterestsResponse> response) {
                Utills.hideLoadingDialog();
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<InterestsResponse.Item> items = response.body().getData().getItems();
                    binding.interestContainer.removeAllViews();

                    // Create category from API list (flat category for now)
                    addCategory("🎮 Gaming Interests", items);

                    // Add Continue button
                    addContinueButton();
                } else {
                    Toast.makeText(IntrestScreenActivity.this, "Failed to load interests", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<InterestsResponse> call, Throwable t) {
                Utills.hideLoadingDialog();
                Toast.makeText(IntrestScreenActivity.this, "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void addContinueButton() {
        ViewGroup parent = (ViewGroup) binding.btnContinue.getParent();
        if (parent != null) {
            parent.removeView(binding.btnContinue);
        }
        binding.interestContainer.addView(binding.btnContinue);

        binding.btnContinue.setOnClickListener(v -> {
            int selectedCount = 0;
            List<String> selectedIds = new ArrayList<>(); // <-- store selected IDs

            for (int i = 0; i < binding.interestContainer.getChildCount(); i++) {
                View categoryView = binding.interestContainer.getChildAt(i);
                ChipGroup chipGroup = categoryView.findViewById(R.id.chip_group);
                if (chipGroup != null) {
                    for (int j = 0; j < chipGroup.getChildCount(); j++) {
                        View chipView = chipGroup.getChildAt(j);
                        if (chipView instanceof Chip) {
                            Chip chip = (Chip) chipView;
                            if (chip.isChecked()) {
                                selectedCount++;
                                selectedIds.add((String) chip.getTag()); // <-- add ID from tag
                            }
                        }
                    }
                }
            }

            if (selectedCount >= 5) {
                // Example: print them
//                Toast.makeText(this, "Selected IDs: " + selectedIds, Toast.LENGTH_LONG).show();

                // Convert list to a comma-separated string
                String idsString = TextUtils.join(",", selectedIds);

                // Save in SharedPreferences
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                prefs.edit().putString("selected_interest_ids", idsString).apply();
                startActivity(new Intent(this, DashboardScreenActivity.class));
            } else {
                Toast.makeText(this, "Please select at least 5 interests", Toast.LENGTH_SHORT).show();
            }
        });

        binding.headerStart.backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onBackPressed();
            }
        });
    }

//    private void Clicks() {
//        if (binding.interestContainer == null) {
//            Toast.makeText(this, "interest_container is NULL", Toast.LENGTH_SHORT).show();
//            return;
//        }
//// Clear any previous views to avoid duplicates
//        binding.interestContainer.removeAllViews();
//        Map<String, List<String>> interestsMap = new LinkedHashMap<>();
//        interestsMap.put("🎮 Gaming", Arrays.asList("VR Games", "Mobile Games", "Console Games", "PC Games", "Indie Games"));
//        interestsMap.put("📦 Collectibles & Hobbies", Arrays.asList("Toys", "Model Kits", "Stamps", "Coins", "Other"));
//        interestsMap.put("💀 Spooky", Arrays.asList("Ghosts", "UFOs", "Myths", "Creepy Stories"));
//        interestsMap.put("🌿 Nature & Outdoors", Arrays.asList("Hiking", "Fishing", "Wildlife", "Camping"));
//        interestsMap.put("🏡 Home & Garden", Arrays.asList("Gardening", "DIY", "Farming", "Interior Design"));
//        interestsMap.put("💬 Communities", Arrays.asList("Reddit", "Discord", "Forums", "Game Dev Meetups"));
//        interestsMap.put("🎨 Creativity", Arrays.asList("Game Design", "Cosplay", "Fan Art", "Machinima"));
//        interestsMap.put("📺 Streaming", Arrays.asList("Twitch", "YouTube", "Kick", "Gameplay Videos"));
//
//        for (Map.Entry<String, List<String>> entry : interestsMap.entrySet()) {
//            addCategory(entry.getKey(), entry.getValue());
//        }
//
//        // Add continue button at the end
//        ViewGroup parent = (ViewGroup) binding.btnContinue.getParent();
//        if (parent != null) {
//            parent.removeView(binding.btnContinue);
//        }
//        binding.interestContainer.addView(binding.btnContinue);
//        binding.btnContinue.setOnClickListener(v -> {
//            int selectedCount = 0;
//
//            // Loop through all categories (children of interestContainer)
//            for (int i = 0; i < binding.interestContainer.getChildCount(); i++) {
//                View categoryView = binding.interestContainer.getChildAt(i);
//                ChipGroup chipGroup = categoryView.findViewById(R.id.chip_group);
//                if (chipGroup != null) {
//                    for (int j = 0; j < chipGroup.getChildCount(); j++) {
//                        View chipView = chipGroup.getChildAt(j);
//                        if (chipView instanceof Chip && ((Chip) chipView).isChecked()) {
//                            selectedCount++;
//                        }
//                    }
//                }
//            }
//
//            if (selectedCount >= 5) {
//                startActivity(new Intent(this, DashboardScreenActivity.class));
//            } else {
//                Toast.makeText(this, "Please select at least 5 interests", Toast.LENGTH_SHORT).show();
//            }
//        });
//
//
//    }

    private void addCategory(String title, List<InterestsResponse.Item> chips) {
        LayoutInflater inflater = LayoutInflater.from(this);
        View categoryView = inflater.inflate(R.layout.item_category, binding.interestContainer, false);

        TextView titleText = categoryView.findViewById(R.id.category_title);
        ChipGroup chipGroup = categoryView.findViewById(R.id.chip_group);

        titleText.setText(title);

        for (InterestsResponse.Item item : chips) {
            Chip chip = new Chip(this);
            chip.setText(item.getLabel());
            chip.setCheckable(true);
            chip.setTextColor(Color.WHITE);
            chip.setChipBackgroundColorResource(R.color.black);
            chip.setChipStrokeWidth(2);
            chip.setChipStrokeColorResource(android.R.color.transparent);
            // 🔹 Store the _id in chip's tag
            chip.setTag(item.getId());
            chipGroup.addView(chip); // Each chip is a new instance!
        }

        binding.interestContainer.addView(categoryView);
    }
}