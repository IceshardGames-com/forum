package com.iceshardgames.gamercommunity.Activity.MainScreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.GravityCompat;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.iceshardgames.gamercommunity.Activity.LoginScreen.LoginScreenActivity;
import com.iceshardgames.gamercommunity.Activity.ProfileScreen.SettingScreenActivity;
import com.iceshardgames.gamercommunity.Adapter.ImageSliderAdapter;
import com.iceshardgames.gamercommunity.Adapter.TabsPagerAdapter;
import com.iceshardgames.gamercommunity.Fragment.CompaniesFragmentBottom;
import com.iceshardgames.gamercommunity.Fragment.EsportsFragmentBottom;
import com.iceshardgames.gamercommunity.Fragment.ForumsFragmentBottom;
import com.iceshardgames.gamercommunity.Fragment.HomeFragmentBottom;
import com.iceshardgames.gamercommunity.Fragment.SearchFragmentBottom;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.SharedPrefManager;
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
        String email = SharedPrefManager.getEmail(this);
        /*if (email != null) {
            emailTextView.setText("Logged in as: " + email);
        }*/


        ViewCompat.setOnApplyWindowInsetsListener(binding.dashboard.parent, (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.dashboard.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment selected = null;

            int itemId = item.getItemId();
            if (itemId == R.id.nav_home) {
                binding.dashboard.header.screenTitleNav.setText(R.string.app_name);
                selected = new HomeFragmentBottom();
            } else if (itemId == R.id.nav_forums) {
                binding.dashboard.header.screenTitleNav.setText("Game Forums");
                selected = new ForumsFragmentBottom();
            }/* else if (itemId == R.id.nav_esports) {
                selected = new EsportsFragmentBottom();
            } else if (itemId == R.id.nav_companies) {
                selected = new CompaniesFragmentBottom();
            }*/ else if (itemId == R.id.nav_chat) {
                binding.dashboard.header.screenTitleNav.setText("Chat here");
                selected = new SearchFragmentBottom();
            }else if (itemId == R.id.nav_search) {
                binding.dashboard.header.screenTitleNav.setText("Search Nexus");
                selected = new SearchFragmentBottom();
            }

            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, selected)
                    .commit();
            return true;
        });

        // Default
        binding.dashboard.bottomNavigation.setSelectedItemId(R.id.nav_home);

        // Profile Drawer menu items
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.profile_menu_items, android.R.layout.simple_list_item_1);
        binding.profileMenu.setAdapter(adapter);
        Utills.GradientText(binding.dashboard.header.screenTitleNav);
        Utills.GradientText(binding.username);

        binding.dashboard.header.notification.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardScreenActivity.this, NotificationScreenActivity.class));
            }
        });

        binding.dashboard.header.setting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(DashboardScreenActivity.this, SettingScreenActivity.class));
            }
        });
        // Open drawer on icon click
        binding.dashboard.header.profile.setOnClickListener(v -> binding.drawerLayout.openDrawer(GravityCompat.END));

        // Close drawer on close icon
//        binding.ivCloseDrawer.setOnClickListener(v -> binding.drawerLayout.closeDrawer(GravityCompat.END));

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedUsername = prefs.getString("username", "u/guest");
        binding.username.setText(savedUsername);
        binding.onlineStatus.setText("Online Status: On");

        binding.profileMenu.setOnItemClickListener((parent, view, position, id) -> {
            String item = (String) parent.getItemAtPosition(position);

            switch (item) {
                case "Logout":
                    // Open ProfileActivity
                    SharedPreferences.Editor editor = prefs.edit();
                    editor.remove("username"); // If you only want to delete username
                    editor.clear(); // Clears isLoggedIn and username
                    editor.apply();

                    Intent intent = new Intent(DashboardScreenActivity.this, LoginScreenActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Prevent back press
                    startActivity(intent);
                    finish();
                    break;
            }

            // Close drawer after click
            binding.drawerLayout.closeDrawer(GravityCompat.END);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String savedUsername = prefs.getString("username", "u/guest");

        binding.username.setText(savedUsername);
    }
}