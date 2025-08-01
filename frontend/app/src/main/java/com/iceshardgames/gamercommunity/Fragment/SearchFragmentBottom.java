package com.iceshardgames.gamercommunity.Fragment;

import static android.view.View.GONE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Activity.LoginScreen.LoginScreenActivity;
import com.iceshardgames.gamercommunity.Activity.MainScreen.NotificationScreenActivity;
import com.iceshardgames.gamercommunity.Activity.ProfileScreen.SettingScreenActivity;
import com.iceshardgames.gamercommunity.Adapter.TrendingAdapter;
import com.iceshardgames.gamercommunity.Model.TrendingTopic;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SearchFragmentBottom extends Fragment {
    private EditText searchBar;

    private LinearLayout recentSearchesList;
    private RecyclerView trendingRecycler;
    private SharedPreferences prefs;
    private List<Button> filterButtons;
    private String currentFilter = "All";
    private List<TrendingTopic> allTopics = new ArrayList<>();
    private List<String> recentSearches;
    public SearchFragmentBottom() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_search, container, false);

        Utills.GradientText(view.findViewById(R.id.title));


        // Initialize views
        recentSearchesList = view.findViewById(R.id.recentSearchesList);
        trendingRecycler = view.findViewById(R.id.trendingRecycler);

        // Initialize list safely
        prefs = getContext().getSharedPreferences("search_prefs", Context.MODE_PRIVATE);
        Set<String> savedSet = prefs.getStringSet("recent_searches", new HashSet<>());
        recentSearches = new ArrayList<>(savedSet);

        loadRecentSearches("");

        // Setup SharedPreferences
        prefs = requireContext().getSharedPreferences("VRNexusPrefs", Context.MODE_PRIVATE);
        currentFilter = prefs.getString("selected_filter", "All");




        // Setup buttons
        Button btnAll = view.findViewById(R.id.btnAll);
        Button btnGames = view.findViewById(R.id.btnGames);
        Button btnPosts = view.findViewById(R.id.btnPosts);
        Button btnUsers = view.findViewById(R.id.btnUsers);
        Button btnCompanies = view.findViewById(R.id.btnCompanies);
        searchBar = view.findViewById(R.id.searchBar);

        filterButtons = Arrays.asList(btnAll, btnGames, btnPosts, btnUsers, btnCompanies);

        // Restore filter selection
        Button savedButton = getButtonByText(currentFilter, filterButtons);
        if (savedButton != null) {
            setSelectedFilter(savedButton, filterButtons);
        }

        // Button click listeners
        btnAll.setOnClickListener(v -> setSelectedFilter(btnAll, filterButtons));
        btnGames.setOnClickListener(v -> setSelectedFilter(btnGames, filterButtons));
        btnPosts.setOnClickListener(v -> setSelectedFilter(btnPosts, filterButtons));
        btnUsers.setOnClickListener(v -> setSelectedFilter(btnUsers, filterButtons));
        btnCompanies.setOnClickListener(v -> setSelectedFilter(btnCompanies, filterButtons));

        // Handle live search
        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performCombinedFilter(s.toString(), currentFilter);
            }
        });

        TextView btnClearRecent = view.findViewById(R.id.clearTitle);
        btnClearRecent.setOnClickListener(v -> {
            // Initialize list safely
            prefs = getContext().getSharedPreferences("search_prefs", Context.MODE_PRIVATE);
            Set<String> savedSet1 = prefs.getStringSet("recent_searches", new HashSet<>());
            recentSearches = new ArrayList<>(savedSet1);

            loadRecentSearches("");
            recentSearchesList.removeAllViews(); // clear from UI
            LinearLayout recentSearchBar = view.findViewById(R.id.recentSearchBar);
            recentSearchBar.setVisibility(GONE);
        });



        // Load topics and recent search data
        setupRecentSearches();
        setupTrendingTopics();


        return view;
    }

    private void setupRecentSearches() {
        recentSearches = Arrays.asList(
                "Half-Life Alyx tips",
                "Beat Saber custom songs",
                "VR headset comparison",
                "Meta Quest 3 review",
                "PSVR2 vs Quest 3",
                "VR horror recommendations"
        );
    }

    private void setupTrendingTopics() {
        allTopics = Arrays.asList(
                new TrendingTopic("VR Fitness Games", "12.5K", "up", "Games"),
                new TrendingTopic("Quest 3 Hand Tracking", "8.7K", "up", "Games"),
                new TrendingTopic("Beat Saber Mods", "15.2K", "flat", "Posts"),
                new TrendingTopic("VR Horror Games", "6.9K", "down", "Games"),
                new TrendingTopic("PSVR2 Exclusives", "9.1K", "up", "Games"),
                new TrendingTopic("Half-Life Alyx Discussion", "7.2K", "up", "Posts"),
                new TrendingTopic("Top VR Creators", "6.1K", "flat", "Users")
        );

        // Initial combined filter with empty query and current filter
        performCombinedFilter("", currentFilter);
    }

    private void performCombinedFilter(String query, String category) {
        if (allTopics == null) return;

        List<TrendingTopic> filteredList = new ArrayList<>();

        for (TrendingTopic topic : allTopics) {
            boolean matchesCategory = category.equalsIgnoreCase("All") || topic.getCategory().equalsIgnoreCase(category);
            boolean matchesQuery = query.isEmpty() || topic.getTitle().toLowerCase().contains(query.toLowerCase());

            if (matchesCategory && matchesQuery) {
                filteredList.add(topic);
            }
        }

        TrendingAdapter adapter = new TrendingAdapter(filteredList);
        trendingRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        trendingRecycler.setAdapter(adapter);
        // ✅ Add this line to update recent searches too
        loadRecentSearches(query);
    }

    private void loadRecentSearches(String keyword) {
        recentSearchesList.removeAllViews();

        for (String search : recentSearches) {
            if (keyword.isEmpty() || search.toLowerCase().contains(keyword.toLowerCase())) {
                TextView tv = new TextView(getContext());
                tv.setText(search);
                tv.setTextColor(getResources().getColor(android.R.color.white));
                tv.setPadding(30, 30, 30, 30);
                tv.setBackgroundResource(R.drawable.button_blur_border_curve); // 👈 new background
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                );
                params.setMargins(0, 0, 0, 30);
                tv.setLayoutParams(params);
                recentSearchesList.addView(tv);
            }
        }
    }

    private void setSelectedFilter(Button selectedButton, List<Button> allButtons) {
        for (Button button : allButtons) {
            button.setSelected(false);
        }
        selectedButton.setSelected(true);

        currentFilter = selectedButton.getText().toString();

        // Save filter to SharedPreferences
        if (prefs != null) {
            prefs.edit().putString("selected_filter", currentFilter).apply();
        }

        // Safe use of searchBar
        String query = searchBar != null ? searchBar.getText().toString() : "";
        performCombinedFilter(query, currentFilter);
    }

    private Button getButtonByText(String text, List<Button> buttons) {
        for (Button b : buttons) {
            if (b.getText().toString().equalsIgnoreCase(text)) return b;
        }
        return null;
    }
}

