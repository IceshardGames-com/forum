package com.iceshardgames.gamercommunity.Fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.iceshardgames.gamercommunity.Activity.LoginScreen.LoginScreenActivity;
import com.iceshardgames.gamercommunity.Activity.MainScreen.NotificationScreenActivity;
import com.iceshardgames.gamercommunity.Model.PostModel;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import android.content.SharedPreferences;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.lang.reflect.Type;

public class ForumDetailFragment extends Fragment {

    private TabLayout tabLayout;
    private RecyclerView postRecyclerView;
    private List<PostModel> allPosts;
    private PostAdapter postAdapter;
    private List<PostModel> filteredPosts;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forum_details, container, false);

        TextView forumTitle = view.findViewById(R.id.forumTitle);
        TextView forumMembers = view.findViewById(R.id.forumMembers);

        Utills.GradientText(view.findViewById(R.id.forumTitle));
        Utills.GradientText(view.findViewById(R.id.header).findViewById(R.id.screen_title_nav));

        view.findViewById(R.id.header).findViewById(R.id.notification).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), NotificationScreenActivity.class));
            }
        });

        view.findViewById(R.id.header).findViewById(R.id.profile).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getActivity(), LoginScreenActivity.class));
            }
        });

        if (getArguments() != null) {
            String title = getArguments().getString("forum_title", "Unknown Forum");
            forumTitle.setText(title);
            forumMembers.setText("8,934 members"); // replace with real value later
        }

        tabLayout = view.findViewById(R.id.forumTabLayout);
        postRecyclerView = view.findViewById(R.id.forumPostRecycler);
        ImageView fabAddPost = view.findViewById(R.id.fabAddPost);

        allPosts = loadPosts();
        // Replace with API/DB call later
        filteredPosts = new ArrayList<>(allPosts);

        postAdapter = new PostAdapter(filteredPosts);
        postRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postRecyclerView.setAdapter(postAdapter);

        setupTabs();
        handleTabSelection();

        fabAddPost.setOnClickListener(v -> {
            Toast.makeText(getContext(), "Add Post Clicked", Toast.LENGTH_SHORT).show();
            showCreateForumDialog();

        });

        return view;
    }

    private void showCreateForumDialog() {
        // TODO: Open create post dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog); // Optional style
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_add_post, null);
        builder.setView(dialogView);

        EditText inputTitle = dialogView.findViewById(R.id.inputPostTitle);
        EditText inputContent = dialogView.findViewById(R.id.inputPostContent);
        TextView btnPost = dialogView.findViewById(R.id.btnPost);

        AlertDialog dialog = builder.create();

        btnPost.setOnClickListener(view1 -> {
            String title = inputTitle.getText().toString().trim();
            String content = inputContent.getText().toString().trim();

            if (!title.isEmpty() && !content.isEmpty()) {
                PostModel newPost = new PostModel(title, "You", 0, 0, true, false);
                allPosts.add(0, newPost); // ✅ Add to top
                savePosts(); // ✅ Save changes
                TabLayout.Tab selectedTab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
                if (selectedTab != null && selectedTab.getCustomView() != null) {
                    TextView tabText = selectedTab.getCustomView().findViewById(R.id.tabText);
                    if (tabText != null) {
                        filterPosts(tabText.getText().toString());
                    }
                }
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "Please enter title and content", Toast.LENGTH_SHORT).show();
            }
        });

        dialog.show();
    }

    private void setupTabs() {
        String[] tabs = {"All\nPosts", "Popular\nPosts", "Recent\nPosts", "Pinned\nPosts", "Pending\nPosts"};
        for (String title : tabs) {
            TabLayout.Tab tab = tabLayout.newTab();
            View customTabView = LayoutInflater.from(getContext()).inflate(R.layout.custom_tab2, null);
            TextView tabText = customTabView.findViewById(R.id.tabText);
            tabText.setText(title);
            // Set default text color
            tabText.setTextColor(ContextCompat.getColor(requireContext(), R.color.lightpink));
            customTabView.setBackgroundResource(R.drawable.tab_default_bg);
            tab.setCustomView(customTabView);
            tabLayout.addTab(tab);
        }
        // Select first tab manually
        if (tabLayout.getTabCount() > 0) {
            tabLayout.getTabAt(0).select();
        }
    }

    private void handleTabSelection() {
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                View customView = tab.getCustomView();
                if (customView != null) {
                    TextView tabTextView = customView.findViewById(R.id.tabText);
                    if (tabTextView != null) {
                        String tabText = tabTextView.getText().toString();
                        filterPosts(tabText);
                    }
                }
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
                View customView = tab.getCustomView();
                if (customView != null) {
                    customView.setSelected(false);
                }
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
                View customView = tab.getCustomView();
                if (customView != null) {
                    TextView tabText = customView.findViewById(R.id.tabText);
                    if (tabText != null) {
                        filterPosts(tabText.getText().toString());
                    }
                }
            }
        });

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
    }

    private void filterPosts(String filter) {
        filteredPosts.clear();

        for (PostModel post : allPosts) {
            switch (filter) {
                case "All\nPosts":
                    filteredPosts.add(post);
                    break;
                case "Popular\nPosts":
                    if (post.getLikes() > 50) filteredPosts.add(post);
                    break;
                case "Recent\nPosts":
                    if (post.isRecent()) filteredPosts.add(post);
                    break;
                case "Pinned\nPosts":
                    if (post.isPinned()) filteredPosts.add(post);
                    break;
                case "Pending\nPosts":
                    if (post.getReplies() == 0) filteredPosts.add(post);
                    break;
            }
        }

        postAdapter.notifyDataSetChanged();
    }

    private List<PostModel> loadPosts() {
        SharedPreferences prefs = getContext().getSharedPreferences("ForumPrefs", getContext().MODE_PRIVATE);
        String json = prefs.getString("forum_posts", null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<PostModel>>() {}.getType();
            return gson.fromJson(json, type);
        } else {
            // Default mock data
            List<PostModel> postList = new ArrayList<>();
            postList.add(new PostModel("Best strategies for new players?", "VRNewbie", 23, 45, true, false));
            postList.add(new PostModel("Updated graphics mod", "ModMaster", 67, 128, true, false));
            postList.add(new PostModel("Looking for co-op partners", "TeamPlayer", 12, 34, false, false));
            postList.add(new PostModel("Bug report: Physics glitch", "BugHunter", 8, 15, false, false));
            postList.add(new PostModel("Achievement tips", "ProGamer", 45, 89, false, true));
            postList.add(new PostModel("Custom maps sharing thread", "MapMaker", 156, 234, false, false));
            return postList;
        }
    }
    private void savePosts() {
        SharedPreferences prefs = getContext().getSharedPreferences("ForumPrefs", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(allPosts);
        editor.putString("forum_posts", json);
        editor.apply();
    }


}
