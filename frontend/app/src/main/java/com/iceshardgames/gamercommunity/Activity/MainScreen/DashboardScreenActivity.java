package com.iceshardgames.gamercommunity.Activity.MainScreen;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.iceshardgames.gamercommunity.Activity.LoginScreen.LoginScreenActivity;
import com.iceshardgames.gamercommunity.Adapter.ImageSliderAdapter;
import com.iceshardgames.gamercommunity.Adapter.TabsPagerAdapter;
import com.iceshardgames.gamercommunity.Fragment.CompaniesFragmentBottom;
import com.iceshardgames.gamercommunity.Fragment.EsportsFragmentBottom;
import com.iceshardgames.gamercommunity.Fragment.ForumsFragmentBottom;
import com.iceshardgames.gamercommunity.Fragment.HomeFragmentBottom;
import com.iceshardgames.gamercommunity.Fragment.SearchFragmentBottom;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;
import com.iceshardgames.gamercommunity.databinding.ActivityDashboardScreenBinding;

import java.util.ArrayList;
import java.util.List;

public class DashboardScreenActivity extends AppCompatActivity {

    ActivityDashboardScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityDashboardScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.parent_container), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selected = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                selected = new HomeFragmentBottom();
            } else if (itemId == R.id.nav_forums) {
                selected = new ForumsFragmentBottom();
            } else if (itemId == R.id.nav_esports) {
                selected = new EsportsFragmentBottom();
            } else if (itemId == R.id.nav_companies) {
                selected = new CompaniesFragmentBottom();
            } else if (itemId == R.id.nav_search) {
                selected = new SearchFragmentBottom();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, selected)
                    .commit();
            return true;
        });

        // Default
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);

    }

}