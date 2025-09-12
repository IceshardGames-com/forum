package com.iceshardgames.gamercommunity.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.text.Layout;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.iceshardgames.gamercommunity.APIintegration.ApiClient;
import com.iceshardgames.gamercommunity.APIintegration.ApiService;
import com.iceshardgames.gamercommunity.Adapter.PostAdapter;
import com.iceshardgames.gamercommunity.Model.PostModel;
import com.iceshardgames.gamercommunity.Model.Request.CreatePostRequest;
import com.iceshardgames.gamercommunity.Model.Response.CreatePostResponse;
import com.iceshardgames.gamercommunity.Model.Response.FollowResponse;
import com.iceshardgames.gamercommunity.Model.Response.GetPostsResponse;
import com.iceshardgames.gamercommunity.Model.Response.JoinResponse;
import com.iceshardgames.gamercommunity.Model.Response.LeaveResponse;
import com.iceshardgames.gamercommunity.Model.Response.UnfollowResponse;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;

import java.util.ArrayList;
import java.util.List;

import android.content.SharedPreferences;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForumDetailFragment extends Fragment {

    private TabLayout tabLayout;
    private RecyclerView postRecyclerView;
    private List<PostModel> allPosts;
    private PostAdapter postAdapter;
    private List<PostModel> filteredPosts;
    String forum_id;
    private boolean isFollowing = false; // track state
    String accessToken;
    private RelativeLayout joinLayout, followLayout;
    private TextView joinText, followText;
    private boolean isJoined = false;
    String postPermission;
    String forum_owner;
    private TextView tvNoPosts;
    String forum_slug = "";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forum_details, container, false);
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        accessToken = prefs.getString("accessToken", null);
        TextView forumTitle = view.findViewById(R.id.forumTitle);
        TextView forumMembers = view.findViewById(R.id.forumMembers);
        LinearLayout FJlayout = view.findViewById(R.id.FJlayout);
        tvNoPosts = view.findViewById(R.id.tv_no_posts);

        Utills.GradientText(view.findViewById(R.id.forumTitle));

        if (getArguments() != null) {
            String title = getArguments().getString("forum_title", "Unknown Forum");
            String forum_status = getArguments().getString("forum_status", "Unknown Forum");
            forum_id = getArguments().getString("forum_id", "Unknown Forum");
            postPermission = getArguments().getString("forum_permission", "admin_only");
            forum_owner = getArguments().getString("forum_owner", "Unknown Forum");
            forum_slug = getArguments().getString("forum_slug", "Unknown Forum"); // fallback to id if slug absent

            forumTitle.setText(title);
            forumMembers.setText(forum_status); // replace with real value later
        }

        tabLayout = view.findViewById(R.id.forumTabLayout);
        followLayout = view.findViewById(R.id.followLayout);
        joinLayout = view.findViewById(R.id.joinLayout);
        followText = view.findViewById(R.id.followText);
        joinText = view.findViewById(R.id.joinText);
        postRecyclerView = view.findViewById(R.id.forumPostRecycler);
        ImageView fabAddPost = view.findViewById(R.id.fabAddPost);

        allPosts = new ArrayList<>();
        filteredPosts = new ArrayList<>();
        postAdapter = new PostAdapter(getActivity(),filteredPosts);
        postRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        postRecyclerView.setAdapter(postAdapter);

        loadPostsFromApi(); // ✅ real API call

        // ---- Session
        String myUserId = prefs.getString("userId", ""); // ensure set at login

        Log.e("==lag", "myUserId: "+myUserId );
        Log.e("==lag", "forum_owner: "+forum_owner );
        Log.e("==lag", "forum_slug: "+forum_slug );
        if(myUserId.equals(forum_owner)){
            FJlayout.setVisibility(View.GONE);
        }else {
            FJlayout.setVisibility(View.VISIBLE);
        }

        TextView shareBtn = view.findViewById(R.id.shareSlug); // or your actual share icon
        shareBtn.setOnClickListener(v -> {
            // replace with your real slug variable
            Log.e("==lag", "forum_slug: "+forum_slug );
            String slug = forum_slug;
            showSlugDialog(slug);
        });


        setupTabs();
        handleTabSelection();

        fabAddPost.setOnClickListener(v -> {

            Log.e("==lag", "postPermission: " + postPermission);

            if(!myUserId.equals(forum_owner)) {
                if (postPermission.equals("followers") && !isFollowing) {
                    Toast.makeText(getContext(), "You must follow this forum to post", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (postPermission.equals("members") && !isJoined) {
                    Toast.makeText(getContext(), "You must join this forum to post", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (postPermission.equals("members")) {
                    // Check paid membership
                    boolean isPaid = prefs.getBoolean("isPaidMember_" + forum_id, false); // you need to set this when verifying payment
                    if (!isPaid) {
                        Toast.makeText(getContext(), "Only paid members can post", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }


            showCreateForumDialog();

        });


        isFollowing = loadFollowState();
        followText.setText(isFollowing ? "Unfollow" : "Follow");

        isJoined = loadJoinState();
        joinText.setText(isJoined ? "Leave" : "Join");

        followLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Utills.showLoadingDialog(getActivity());
                followAPI();
            }
        });

        joinLayout.setOnClickListener(v -> {
            Utills.showLoadingDialog(getActivity());
            joinAPI();
        });

        return view;
    }

    private void joinAPI() {
        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);

        if (!isJoined) {
            // Join API
            apiService.joinForum("Bearer " + accessToken, forum_id)
                    .enqueue(new Callback<JoinResponse>() {
                        @Override
                        public void onResponse(Call<JoinResponse> call, Response<JoinResponse> response) {
                            Utills.hideLoadingDialog();
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                isJoined = true;
                                joinText.setText("Leave");
                                saveJoinState(true);  // ✅ Save state
                                Toast.makeText(getContext(), "Joined forum", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to join", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<JoinResponse> call, Throwable t) {
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> {
                                    Utills.hideLoadingDialog();
                                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    });

        } else {
            // Leave API
            apiService.leaveForum("Bearer " + accessToken, forum_id)
                    .enqueue(new Callback<LeaveResponse>() {
                        @Override
                        public void onResponse(Call<LeaveResponse> call, Response<LeaveResponse> response) {
                            Utills.hideLoadingDialog();
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                isJoined = false;
                                joinText.setText("Join");
                                saveJoinState(false); // ✅ Save state
                                Toast.makeText(getContext(), "Left forum", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to leave", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<LeaveResponse> call, Throwable t) {
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> {
                                    Utills.hideLoadingDialog();
                                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    });
        }
    }


    private void followAPI() {
        if (!isFollowing) {
            // Call Follow API

            ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
            apiService.followForum("Bearer " + accessToken, forum_id).enqueue(new Callback<FollowResponse>() {
                @Override
                public void onResponse(Call<FollowResponse> call, Response<FollowResponse> response) {
                    Utills.hideLoadingDialog();
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        isFollowing = true;
                        followText.setText("Unfollow");
                        saveFollowState(true);  // ✅ Save state
                        Toast.makeText(getContext(), "Followed forum", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to follow", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<FollowResponse> call, Throwable t) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Utills.hideLoadingDialog();
                            Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        } else {
            ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
            apiService.unfollowForum("Bearer " + accessToken, forum_id).enqueue(new Callback<UnfollowResponse>() {
                @Override
                public void onResponse(Call<UnfollowResponse> call, Response<UnfollowResponse> response) {
                    Utills.hideLoadingDialog();
                    if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                        isFollowing = false;
                        followText.setText("Follow");
                        saveFollowState(false); // ✅ Save state
                        Toast.makeText(getContext(), "Unfollowed forum", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "Failed to unfollow", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<UnfollowResponse> call, Throwable t) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Utills.hideLoadingDialog();
                            Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                        });
                    }
                }
            });
        }
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

//        btnPost.setOnClickListener(view1 -> {
//            String title = inputTitle.getText().toString().trim();
//            String content = inputContent.getText().toString().trim();
//
//            if (title.isEmpty() || content.isEmpty()) {
//                Toast.makeText(getContext(), "Please enter title and content", Toast.LENGTH_SHORT).show();
//                return;
//            }
//
//            if (!title.isEmpty() && !content.isEmpty()) {
//                PostModel newPost = new PostModel(title, "You", 0, 0, true, false);
//                allPosts.add(0, newPost); // ✅ Add to top
//                savePosts(); // ✅ Save changes
//                TabLayout.Tab selectedTab = tabLayout.getTabAt(tabLayout.getSelectedTabPosition());
//                if (selectedTab != null && selectedTab.getCustomView() != null) {
//                    TextView tabText = selectedTab.getCustomView().findViewById(R.id.tabText);
//                    if (tabText != null) {
//                        filterPosts(tabText.getText().toString());
//                    }
//                }
//                dialog.dismiss();
//            } else {
//                Toast.makeText(getContext(), "Please enter title and content", Toast.LENGTH_SHORT).show();
//            }
//        });
        btnPost.setOnClickListener(view1 -> {
            String title = inputTitle.getText().toString().trim();
            String content = inputContent.getText().toString().trim();

            if (title.isEmpty() || content.isEmpty()) {
                Toast.makeText(getContext(), "Please enter title and content", Toast.LENGTH_SHORT).show();
                return;
            }

            Utills.showLoadingDialog(getActivity());
            ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
            CreatePostRequest body = new CreatePostRequest(title, content);

            apiService.createPost("Bearer " + accessToken, forum_id, body)
                    .enqueue(new Callback<CreatePostResponse>() {
                        @Override
                        public void onResponse(Call<CreatePostResponse> call, Response<CreatePostResponse> response) {
                            Utills.hideLoadingDialog();
                            if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                                CreatePostResponse.Post newPost = response.body().getData().getPost();
                                // assuming your API model has: newPost.getId(), getCreatedAt() ISO string
                                long createdAtMillis = Utills.parseServerTimeToMillis(newPost.getCreatedAt());
                                if (createdAtMillis == 0L) createdAtMillis = System.currentTimeMillis();
                                PostModel postModel = new PostModel(
                                        newPost.getId(),
                                        newPost.getTitle(),
                                        newPost.getContent(),
                                        newPost.getLikes(),
                                        newPost.getDislikes(),
                                        true,
                                        false,
                                        createdAtMillis,
                                        newPost.getLikes(),
                                        newPost.getAuthor()
                                );

                                allPosts.add(0, postModel);
                                filteredPosts.clear();
                                filteredPosts.addAll(allPosts);
                                postAdapter.notifyDataSetChanged();
                                dialog.dismiss();
                                loadPostsFromApi(); // ✅ real API call
                                Toast.makeText(getContext(), "Post created", Toast.LENGTH_SHORT).show();
                            } else {
                                Toast.makeText(getContext(), "Failed to create post", Toast.LENGTH_SHORT).show();
                            }
                        }

                        @Override
                        public void onFailure(Call<CreatePostResponse> call, Throwable t) {
                            if (isAdded()) {
                                requireActivity().runOnUiThread(() -> {
                                    Utills.hideLoadingDialog();
                                    Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                });
                            }
                        }
                    });
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

/*
    private List<PostModel> loadPosts() {
        SharedPreferences prefs = getContext().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE);
        String json = prefs.getString("forum_posts", null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<PostModel>>() {
            }.getType();
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
*/

    private void loadPostsFromApi() {
        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        Log.e("==lag", "forum_id: " + forum_id);
        Log.e("==lag", "accessToken: " + accessToken);

        apiService.getPosts("Bearer " + accessToken, forum_id)
                .enqueue(new Callback<GetPostsResponse>() {
                    @Override
                    public void onResponse(Call<GetPostsResponse> call, Response<GetPostsResponse> response) {
                        if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                            allPosts.clear();
                            for (CreatePostResponse.Post post : response.body().getData().getPosts()) {
                                long createdAtMillis = Utills.parseServerTimeToMillis(post.getCreatedAt());
                                if (createdAtMillis == 0L) {
                                    // as a last resort, do NOT set to now; leave 0 or some safe default
                                    // but better to keep server time correct
                                }
                                allPosts.add(new PostModel(
                                        post.getId(),
                                        post.getTitle(),
                                        post.getContent(),
                                        post.getLikes(),
                                        post.getDislikes(),
                                        false,
                                        false,
                                        createdAtMillis,
                                        post.getLikes(),
                                        post.getAuthor()
                                ));
                            }
                            filteredPosts.clear();
                            filteredPosts.addAll(allPosts);
                            postAdapter.notifyDataSetChanged();

                            // ✅ Show "No posts available" if empty
                            if (filteredPosts.isEmpty()) {
                                tvNoPosts.setVisibility(View.VISIBLE);
                                postRecyclerView.setVisibility(View.GONE);
                            } else {
                                tvNoPosts.setVisibility(View.GONE);
                                postRecyclerView.setVisibility(View.VISIBLE);
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to load posts", Toast.LENGTH_SHORT).show();
                            tvNoPosts.setVisibility(View.VISIBLE);
                            postRecyclerView.setVisibility(View.GONE);
                        }
                    }

                    @Override
                    public void onFailure(Call<GetPostsResponse> call, Throwable t) {
                        if (isAdded()) {
                            requireActivity().runOnUiThread(() -> {
                                Toast.makeText(requireActivity(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                                tvNoPosts.setVisibility(View.VISIBLE);
                                postRecyclerView.setVisibility(View.GONE);
                            });
                        }
                    }
                });
    }

    private void savePosts() {
        SharedPreferences prefs = getContext().getSharedPreferences("UserPrefs", getContext().MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        Gson gson = new Gson();
        String json = gson.toJson(allPosts);
        editor.putString("forum_posts", json);
        editor.apply();
    }

    private void saveFollowState(boolean state) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isFollowing_" + forum_id, state);
        editor.apply();
    }

    private void saveJoinState(boolean state) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putBoolean("isJoined_" + forum_id, state);
        editor.apply();
    }

    private boolean loadFollowState() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getBoolean("isFollowing_" + forum_id, false);
    }

    private boolean loadJoinState() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        return prefs.getBoolean("isJoined_" + forum_id, false);
    }

    private void showSlugDialog(String slugToShare) {
        if (!isAdded()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.CustomDialog);
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_share_slug, null);
        builder.setView(dialogView);

        TextView slugText = dialogView.findViewById(R.id.slug_text);
        slugText.setText(slugToShare);

        ImageView btnCopy = dialogView.findViewById(R.id.btn_copy);
        ImageView btnShare = dialogView.findViewById(R.id.btn_share);

        AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();

        // Optional: adjust width
        if (dialog.getWindow() != null) {
            int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92);
            dialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }

        btnCopy.setOnClickListener(v -> {
            copyTextToClipboard(slugToShare);
            Toast.makeText(getContext(), "Copied", Toast.LENGTH_SHORT).show();
        });

        btnShare.setOnClickListener(v -> {
            shareText(slugToShare);
        });
    }

    private void copyTextToClipboard(String text) {
        ClipboardManager clipboard = (ClipboardManager) requireActivity().getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("forum_slug", text);
        clipboard.setPrimaryClip(clip);
    }

    private void shareText(String text) {
        Intent sendIntent = new Intent(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, text);
        sendIntent.setType("text/plain");

        Intent chooser = Intent.createChooser(sendIntent, "Share forum slug");
        startActivity(chooser);
    }


}
