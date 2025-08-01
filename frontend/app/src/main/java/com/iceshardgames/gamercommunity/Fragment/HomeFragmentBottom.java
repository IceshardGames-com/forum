package com.iceshardgames.gamercommunity.Fragment;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;
import com.iceshardgames.gamercommunity.Activity.LoginScreen.LoginScreenActivity;
import com.iceshardgames.gamercommunity.Activity.MainScreen.NotificationScreenActivity;
import com.iceshardgames.gamercommunity.Activity.ProfileScreen.SettingScreenActivity;
import com.iceshardgames.gamercommunity.Adapter.ImageSliderAdapter;
import com.iceshardgames.gamercommunity.Adapter.TabsPagerAdapter;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;
import com.iceshardgames.gamercommunity.databinding.FragmentHomeBinding;

import java.util.ArrayList;
import java.util.List;

public class HomeFragmentBottom extends Fragment {
    private Handler sliderHandler = new Handler(Looper.getMainLooper());
    private int currentPosition = 0;
    private final int SLIDE_DELAY = 2000; // 3 seconds
    private FragmentHomeBinding binding;

    private final Runnable sliderRunnable = new Runnable() {
        @Override
        public void run() {
            if (binding != null && binding.imageSlider.getAdapter() != null) {
                int itemCount = binding.imageSlider.getAdapter().getItemCount();
                currentPosition = (currentPosition + 1) % itemCount;
                binding.imageSlider.setCurrentItem(currentPosition, true);
                sliderHandler.postDelayed(this, SLIDE_DELAY);
            }
        }
    };


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);



        List<Integer> imageList = new ArrayList<>();
        imageList.add(R.drawable.img_slider1);
        imageList.add(R.drawable.img_slider3);
        imageList.add(R.drawable.img_slider4);
        imageList.add(R.drawable.img_slider5);
        imageList.add(R.drawable.img_slider6);
        imageList.add(R.drawable.img_slider7);
        imageList.add(R.drawable.img_slider8);
        imageList.add(R.drawable.img_slider9);
        imageList.add(R.drawable.img_slider10);
        imageList.add(R.drawable.img_slider11);

        ImageSliderAdapter adapter = new ImageSliderAdapter(requireContext(), imageList);
        binding.imageSlider.setAdapter(adapter);
        sliderHandler.postDelayed(sliderRunnable, SLIDE_DELAY);
        setupTabs();
    }

    private void setupTabs() {
        TabsPagerAdapter adapter = new TabsPagerAdapter(requireActivity());
        binding.viewPager.setAdapter(adapter);

        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            View customView = LayoutInflater.from(getContext()).inflate(R.layout.custom_tab, null);
            TextView tabText = customView.findViewById(R.id.tabText);

            switch (position) {
                case 0:
                    tabText.setText("Trending");
                    break;
                case 1:
                    tabText.setText("Games");
                    break;
                case 2:
                    tabText.setText("Forums");
                    break;
            }

            // Set default text color
            tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightpink));
            customView.setBackgroundResource(R.drawable.tab_default_bg);

            tab.setCustomView(customView);
        }).attach();

        binding.tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
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
        TabLayout.Tab defaultTab = binding.tabLayout.getTabAt(0);
        if (defaultTab != null && defaultTab.getCustomView() != null) {
            View customView = defaultTab.getCustomView();
            customView.setBackgroundResource(R.drawable.button_round_background);
            TextView tabText = customView.findViewById(R.id.tabText);
            tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.white));
            binding.tabLayout.selectTab(defaultTab);
        }

        sliderHandler.postDelayed(sliderRunnable, SLIDE_DELAY);
    }

    @Override
    public void onResume() {
        super.onResume();
        sliderHandler.postDelayed(sliderRunnable, SLIDE_DELAY);
    }

    @Override
    public void onPause() {
        super.onPause();
        sliderHandler.removeCallbacks(sliderRunnable);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        sliderHandler.removeCallbacks(sliderRunnable);
        binding = null;
    }
}

