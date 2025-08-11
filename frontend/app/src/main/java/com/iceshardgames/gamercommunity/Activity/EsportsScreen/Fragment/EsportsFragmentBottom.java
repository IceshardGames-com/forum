package com.iceshardgames.gamercommunity.Activity.EsportsScreen.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.iceshardgames.gamercommunity.Activity.EsportsScreen.Adapter.EsportsPagerAdapter;
import com.iceshardgames.gamercommunity.R;

public class EsportsFragmentBottom extends Fragment {
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private EsportsPagerAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_esports, container, false);

        tabLayout = view.findViewById(R.id.esportsTabLayout);
        viewPager = view.findViewById(R.id.esportsViewPager);

        adapter = new EsportsPagerAdapter(this);
        viewPager.setAdapter(adapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            View customView = LayoutInflater.from(getContext()).inflate(R.layout.custom_tab, null);
            TextView tabText = customView.findViewById(R.id.tabText);

            switch (position) {
                case 0:
                    tabText.setText("Live");
                    break;
                case 1:
                    tabText.setText("Upcoming");
                    break;
                case 2:
                    tabText.setText("Completed");
                    break;
                case 3:
                    tabText.setText("Teams");
                    break;

            }

            // Set default text color
            tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightpink));
            tabText.setTextSize(8);
            tabText.setPadding(30,0,30,0);
            customView.setBackgroundResource(R.drawable.tab_default_bg);

            tab.setCustomView(customView);
        }).attach();

        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                if (tab.getCustomView() != null) {
                    View customView = tab.getCustomView();
                    customView.setBackgroundResource(R.drawable.button_round_background);
                    TextView tabText = customView.findViewById(R.id.tabText);
                    tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                if (tab.getCustomView() != null) {
                    View customView = tab.getCustomView();
                    customView.setBackgroundResource(R.drawable.tab_default_bg);
                    TextView tabText = customView.findViewById(R.id.tabText);
                    tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightpink));
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                // Optional: handle reselection if needed
            }
        });

        // Select Trending Tab
        TabLayout.Tab defaultTab = tabLayout.getTabAt(0);
        if (defaultTab != null && defaultTab.getCustomView() != null) {
            View customView = defaultTab.getCustomView();
            customView.setBackgroundResource(R.drawable.button_round_background);
            TextView tabText = customView.findViewById(R.id.tabText);
            tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            tabLayout.selectTab(defaultTab);
        }

        return view;
    }
}
