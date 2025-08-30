package com.iceshardgames.gamercommunity.Activity.MainScreen;

import static android.view.View.GONE;
import static android.view.View.TEXT_ALIGNMENT_CENTER;

import android.content.Intent;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.iceshardgames.gamercommunity.Activity.LoginScreen.LoginScreenActivity;
import com.iceshardgames.gamercommunity.Activity.ProfileScreen.SettingScreenActivity;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;
import com.iceshardgames.gamercommunity.databinding.ActivityNotificationScreenBinding;

public class NotificationScreenActivity extends AppCompatActivity {

    ActivityNotificationScreenBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityNotificationScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Utills.GradientText(binding.header.screenTitleNav);
        binding.header.notification.setVisibility(GONE);
        binding.header.setting.setVisibility(GONE);
        binding.header.profile.setVisibility(GONE);

        InboxPagerAdapter adapter = new InboxPagerAdapter(this);
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            if (position == 0) tab.setText("Notifications");
            else tab.setText("Messages");
        }).attach();

        // After attaching the TabLayoutMediator
        for (int i = 0; i < binding.tabLayout.getTabCount(); i++) {
            TabLayout.Tab tab = binding.tabLayout.getTabAt(i);
            if (tab != null) {
                TextView tabTextView = new TextView(this);
                tabTextView.setText(tab.getText());
                tabTextView.setTextSize(14); // Set your text size
                tabTextView.setGravity(Gravity.CENTER);
                // For even better centering, you can set layout parameters
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        LinearLayout.LayoutParams.MATCH_PARENT
                );
                tabTextView.setLayoutParams(params);
                tabTextView.setTypeface(ResourcesCompat.getFont(this, R.font.titilliumweb_bold));
                tabTextView.setTextColor(ContextCompat.getColorStateList(this, R.color.tab_text_selector));
                tab.setCustomView(tabTextView);
            }
        }
    }
}