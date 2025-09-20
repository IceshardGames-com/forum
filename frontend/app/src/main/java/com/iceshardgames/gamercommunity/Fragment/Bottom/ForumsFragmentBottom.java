package com.iceshardgames.gamercommunity.Fragment.Bottom;

import static android.content.Context.MODE_PRIVATE;

import com.github.slugify.Slugify;

import android.app.AlertDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.Settings;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.core.content.res.ResourcesCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.button.MaterialButtonToggleGroup;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.iceshardgames.gamercommunity.APIintegration.ApiClient;
import com.iceshardgames.gamercommunity.APIintegration.ApiService;
import com.iceshardgames.gamercommunity.Adapter.ForumAdapter;
import com.iceshardgames.gamercommunity.Model.ForumModel;
import com.iceshardgames.gamercommunity.Model.Request.CreateForumRequest;
import com.iceshardgames.gamercommunity.Model.Response.CreateForumResponse;
import com.iceshardgames.gamercommunity.Model.Response.ForumBySlugResponse;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.text.Normalizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ForumsFragmentBottom extends Fragment {
    private EditText searchBar;
    private RecyclerView forumRecycler;
    private List<ForumModel> allForums = new ArrayList<>();
    private List<Button> filterButtons;
    private String currentFilter = "All";
    private SharedPreferences prefs;
    private AlertDialog currentDialog = null;
    private AlertDialog currentDialog2 = null;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forums_bottom, container, false);

        Utills.GradientText(view.findViewById(R.id.title));


        ImageView btnCreateForum = view.findViewById(R.id.fabAddForum);
        btnCreateForum.setOnClickListener(v -> {
//            showCreateForumDialog();
            showFabOptionsDialog();
        });

        // SharedPreferences for saving filter state
        prefs = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String saved = prefs.getString("selected_filter", "All");
        currentFilter = saved.equalsIgnoreCase("All Games") ? "All" : saved;

        // Init Views
        searchBar = view.findViewById(R.id.searchBar);
        forumRecycler = view.findViewById(R.id.forumRecycler);

        Button btnAllGames = view.findViewById(R.id.btnAllGames);
        Button btnActionRpg = view.findViewById(R.id.btnActionRpg);
        Button btnActionAdventure = view.findViewById(R.id.btnActionAdventure);
        Button btnBattleRoyale = view.findViewById(R.id.btnBattleRoyale);
        Button btnFighting = view.findViewById(R.id.btnFighting);
        Button btnFirstPersonShooter = view.findViewById(R.id.btnFirstPersonShooter);
        Button btnJrpg = view.findViewById(R.id.btnJrpg);
        Button btnMetroidvania = view.findViewById(R.id.btnMetroidvania);
        Button btnMmorpg = view.findViewById(R.id.btnMmorpg);
        Button btnMoba = view.findViewById(R.id.btnMoba);
        Button btnOpenWorld = view.findViewById(R.id.btnOpenWorld);
        Button btnParty = view.findViewById(R.id.btnParty);
        Button btnPlatformer = view.findViewById(R.id.btnPlatformer);
        Button btnRoguelike = view.findViewById(R.id.btnRoguelike);
        Button btnRolePlaying = view.findViewById(R.id.btnRolePlaying);
        Button btnRpg = view.findViewById(R.id.btnRpg);
        Button btnSandbox = view.findViewById(R.id.btnSandbox);
        Button btnSimulation = view.findViewById(R.id.btnSimulation);
        Button btnSports = view.findViewById(R.id.btnSports);
        Button btnStrategyRpg = view.findViewById(R.id.btnStrategyRpg);

        filterButtons = Arrays.asList(btnAllGames,
                btnActionRpg,
                btnActionAdventure,
                btnBattleRoyale,
                btnFighting,
                btnFirstPersonShooter,
                btnJrpg,
                btnMetroidvania,
                btnMmorpg,
                btnMoba,
                btnOpenWorld,
                btnParty,
                btnPlatformer,
                btnRoguelike,
                btnRolePlaying,
                btnRpg,
                btnSandbox,
                btnSimulation,
                btnSports,
                btnStrategyRpg
        );

        // Set background selector style
        for (Button b : filterButtons) {
            b.setBackgroundResource(R.drawable.category_button_bg);
        }

        // Restore selected filter state
        Button savedButton = getButtonByText(currentFilter, filterButtons);
        if (savedButton != null) {
            setSelectedFilter(savedButton, filterButtons);
        }


        // Set click listeners
        btnAllGames.setOnClickListener(v -> {
            setSelectedFilter(btnAllGames, filterButtons);
            filterAndDisplayForums("All");

        });
        btnActionRpg.setOnClickListener(v -> setSelectedFilter(btnActionRpg, filterButtons));
        btnActionAdventure.setOnClickListener(v -> setSelectedFilter(btnActionAdventure, filterButtons));
        btnBattleRoyale.setOnClickListener(v -> setSelectedFilter(btnBattleRoyale, filterButtons));
        btnFighting.setOnClickListener(v -> setSelectedFilter(btnFighting, filterButtons));
        btnFirstPersonShooter.setOnClickListener(v -> setSelectedFilter(btnFirstPersonShooter, filterButtons));
        btnJrpg.setOnClickListener(v -> setSelectedFilter(btnJrpg, filterButtons));
        btnMetroidvania.setOnClickListener(v -> setSelectedFilter(btnMetroidvania, filterButtons));
        btnMmorpg.setOnClickListener(v -> setSelectedFilter(btnMmorpg, filterButtons));
        btnMoba.setOnClickListener(v -> setSelectedFilter(btnMoba, filterButtons));
        btnOpenWorld.setOnClickListener(v -> setSelectedFilter(btnOpenWorld, filterButtons));
        btnParty.setOnClickListener(v -> setSelectedFilter(btnParty, filterButtons));
        btnPlatformer.setOnClickListener(v -> setSelectedFilter(btnPlatformer, filterButtons));
        btnRoguelike.setOnClickListener(v -> setSelectedFilter(btnRoguelike, filterButtons));
        btnRolePlaying.setOnClickListener(v -> setSelectedFilter(btnRolePlaying, filterButtons));
        btnRpg.setOnClickListener(v -> setSelectedFilter(btnRpg, filterButtons));
        btnSandbox.setOnClickListener(v -> setSelectedFilter(btnSandbox, filterButtons));
        btnSimulation.setOnClickListener(v -> setSelectedFilter(btnSimulation, filterButtons));
        btnSports.setOnClickListener(v -> setSelectedFilter(btnSports, filterButtons));
        btnStrategyRpg.setOnClickListener(v -> setSelectedFilter(btnStrategyRpg, filterButtons));

        searchBar.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                performLiveSearch(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

//        setupForumList();
        // Ensure selection + filtering happens after layout is ready
        btnAllGames.post(() -> {
            setSelectedFilter(btnAllGames, filterButtons);
            filterAndDisplayForums("All");
        });
        loadForums();

        filterAndDisplayForums(currentFilter);

        return view;
    }

    private void showFabOptionsDialog() {
        // Prevent stacking dialogs
        if (currentDialog != null && currentDialog.isShowing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.CustomDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_fab_options, null);
        builder.setView(dialogView);

        // Find views
        LinearLayout optionCreate = dialogView.findViewById(R.id.option_create);
        LinearLayout optionJoin = dialogView.findViewById(R.id.option_join);
        TextView cancel = dialogView.findViewById(R.id.dialog_cancel);
        TextView dialog_title = dialogView.findViewById(R.id.dialog_title);

        Utills.GradientText(dialog_title);
        // Create and show
        currentDialog = builder.create();
        currentDialog.setCanceledOnTouchOutside(true);

        // Click handlers
        optionCreate.setOnClickListener(v -> {
            if (currentDialog != null && currentDialog.isShowing()) currentDialog.dismiss();
            // small delay can help avoid UI overlap (optional)
            showCreateForumDialog();
        });

        optionJoin.setOnClickListener(v -> {
            if (currentDialog != null && currentDialog.isShowing()) currentDialog.dismiss();
            showJoinForumDialog();
        });

        cancel.setOnClickListener(v -> {
            if (currentDialog != null && currentDialog.isShowing()) currentDialog.dismiss();
        });

        currentDialog.setOnDismissListener(d -> currentDialog = null);
        currentDialog.show();

        // Optionally set dialog width to match parent - small margin
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92);
        if (currentDialog.getWindow() != null) {
            currentDialog.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }

    private void loadForums() {
        String json = prefs.getString("forums_list", null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<ForumModel>>() {}.getType();
            allForums = gson.fromJson(json, type);

            // 🔑 Clear localNew flag if the forum is older than 24h
            for (ForumModel fm : allForums) {
                if (!fm.isNew()) {
                    fm.setLocalNew(false);
                }
            }
        } else {
            allForums = new ArrayList<>();
        }
    }


    private void performLiveSearch(String query) {
        List<ForumModel> filtered = new ArrayList<>();
        for (ForumModel model : allForums) {
            if ((currentFilter.equals("All") || model.getCategory().equalsIgnoreCase(currentFilter)) &&
                    model.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(model);
            }
        }

        ForumAdapter adapter = new ForumAdapter(getActivity(), filtered);
        if (forumRecycler.getLayoutManager() == null) {
            forumRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        forumRecycler.setAdapter(adapter);
    }


   /* private void setupForumList() {
        allForums = new ArrayList<>(Arrays.asList(
                new ForumModel("Half-Life: Alyx", "2.5K • 234 posts", "Last: 2 min ago", "Active", "Action", R.drawable.forum1),
                new ForumModel("Beat Saber", "5.2K • 456 posts", "Last: 5 min ago", "Active", "Rhythm", R.drawable.forum2),
                new ForumModel("Boneworks", "1.8K • 178 posts", "Last: 12 min ago", "Quiet", "Action", R.drawable.forum3),
                new ForumModel("Synth Riders", "3.9K • 321 posts", "Last: 7 min ago", "Active", "Rhythm", R.drawable.forum4),
                new ForumModel("Superhot VR", "2.2K • 201 posts", "Last: 9 min ago", "Active", "Shooter", R.drawable.forum5),
                new ForumModel("Pistol Whip", "3.4K • 280 posts", "Last: 4 min ago", "Active", "Shooter", R.drawable.forum6),
                new ForumModel("Resident Evil 4 VR", "4.7K • 402 posts", "Last: 3 min ago", "Hot", "Horror", R.drawable.forum4),
                new ForumModel("Phasmophobia VR", "3.1K • 310 posts", "Last: 6 min ago", "Active", "Horror", R.drawable.forum3)
        ));

        filterAndDisplayForums("All");

    }*/


    private void filterAndDisplayForums(String category) {
        List<ForumModel> filtered = new ArrayList<>();
        for (ForumModel model : allForums) {
            if (category.equalsIgnoreCase("All") || model.getCategory().equalsIgnoreCase(category)) {
                filtered.add(model);
            }
        }

        ForumAdapter adapter = new ForumAdapter(getActivity(), filtered);
        forumRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        forumRecycler.setAdapter(adapter);
    }

    private void setSelectedFilter(Button selectedButton, List<Button> allButtons) {
        for (Button button : allButtons) {
            button.setSelected(false);
        }
        selectedButton.setSelected(true);

        String selectedText = selectedButton.getText().toString();
        currentFilter = selectedText.equalsIgnoreCase("All Games") ? "All" : selectedText;

        if (prefs != null) {
            prefs.edit().putString("selected_filter", currentFilter).apply();
        }
// Apply filter + search
        performLiveSearch(searchBar.getText().toString());
    }

    private Button getButtonByText(String text, List<Button> buttons) {
        String normalized = text.equalsIgnoreCase("All") ? "All Games" : text;
        for (Button b : buttons) {
            if (b.getText().toString().equalsIgnoreCase(normalized)) return b;
        }
        return null;
    }

    String permission = "admin_only";

    private void showCreateForumDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity(), R.style.CustomDialog); // Optional style
        LayoutInflater inflater = getLayoutInflater();
        View view = inflater.inflate(R.layout.activity_create_forum, null);
        builder.setView(view);
        AlertDialog dialog = builder.create();
        dialog.show();
        Utills.GradientText(view.findViewById(R.id.header));
        EditText titleInput = view.findViewById(R.id.editForumTitle);
        EditText descInput = view.findViewById(R.id.editForumDescription);
        TextView charCount = view.findViewById(R.id.charCountText);
        Spinner categorySpinner = view.findViewById(R.id.spinnerCategory);
        CheckBox privateCheckbox = view.findViewById(R.id.checkboxPrivateForum);
        TextView cancelBtn = view.findViewById(R.id.btnCancel);
        TextView createBtn = view.findViewById(R.id.btnCreateForum);
        ImageView close = view.findViewById(R.id.close);

        MaterialButtonToggleGroup toggleGroup = view.findViewById(R.id.permissionToggleGroup);
        toggleGroup.addOnButtonCheckedListener((group, checkedId, isChecked) -> {
            if (isChecked) {
                MaterialButton selectedButton = view.findViewById(checkedId);
                if (checkedId == R.id.btnAdminOnly) {
                    permission = "admin_only";
                } else if (checkedId == R.id.btnFollowers) {
                    permission = "followers";
                } else if (checkedId == R.id.btnMembers) {
                    permission = "members";
                }
                Log.d("==ForumPermission", "Selected: " + permission);
            }
        });


        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        // Category options (you can load these dynamically too)
        String[] categories = {"Action Rpg", "Action Adventure", "Battle Royale", "Fighting", "First Person Shooter", "Jrpg",
                "Metroidvania", "Mmorpg", "Moba", "Open World", "Party", "Platformer",
                "Roguelike", "Role Playing", "Rpg", "Sandbox", "Simulation", "Sports", "Strategy Rpg"};
        ArrayAdapter<String> adapter = new ArrayAdapter<String>(getActivity(), android.R.layout.simple_spinner_item, categories) {
            @NonNull
            @Override
            public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getView(position, convertView, parent);
                styleTextView(view);
                return view;
            }

            @Override
            public View getDropDownView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
                TextView view = (TextView) super.getDropDownView(position, convertView, parent);
                styleTextView(view);
                return view;
            }

            private void styleTextView(TextView view) {
                view.setTextColor(ContextCompat.getColor(getActivity(), R.color.black));
                view.setTextSize(9); // in sp
                view.setTextColor(getResources().getColor(R.color.white));
                view.setTypeface(ResourcesCompat.getFont(getActivity(), R.font.titilliumweb_raguler)); // Your font in res/font
                view.setPadding(50, 16, 16, 16);
            }
        };
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        categorySpinner.setAdapter(adapter);

        // Update character count
        descInput.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                charCount.setText(s.length() + "/500 characters");
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Cancel button
        cancelBtn.setOnClickListener(v -> dialog.dismiss());

        // Create Forum button
        createBtn.setOnClickListener(v -> {
            String title = titleInput.getText().toString().trim();
            String description = descInput.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();
            boolean isPrivate = privateCheckbox.isChecked();

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }
            Utills.showLoadingDialog(getActivity());

            String accessToken = prefs.getString("accessToken", null);
            Log.e("==lag", "token : " + accessToken);

            // 1) Build backend-compliant base slug
            String baseSlug = Slugs.makeSlug(title); // lowercase, only a-z0-9- and length 3..64

            // 2) Create a short suffix (android id or random), then unique slug
            String shortId = Slugs.shortIdFromAndroidIdOrRandom(getContext()); // e.g. "4f9a82c1"
            String slug = Slugs.uniqueSlug(baseSlug, shortId); // base + "-" + shortId (max length respected)

            Log.e("==lag", "slug : " + slug);

            boolean verified = false;

            ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
            CreateForumRequest body = new CreateForumRequest(
                    title,
                    slug,
                    description,
                    verified,
                    permission
            );

            Log.e("==lag", "token: " + "Bearer " + accessToken);
            Log.e("==lag", "title: " + title);
            Log.e("==lag", "slug: " + slug);
            Log.e("==lag", "description: " + description);
            Log.e("==lag", "verified: " + verified);
            Log.e("==lag", "postPermission: " + permission);

            apiService.createForum("Bearer " + accessToken, body).enqueue(new Callback<CreateForumResponse>() {
                @Override
                public void onResponse(Call<CreateForumResponse> call, Response<CreateForumResponse> response) {
                    createBtn.setEnabled(true);

                    if (!response.isSuccessful()) {
                        try {
                            String errorBody = response.errorBody().string();
                            JSONObject obj = new JSONObject(errorBody);

                            if (obj.has("error")) {
                                JSONObject error = obj.getJSONObject("error");
                                String code = error.getString("code");
                                if ("DUPLICATE_ERROR".equals(code) && error.getJSONObject("details").getString("field").equals("slug")) {
                                    // Get the duplicate slug
                                    String duplicateSlug = error.getJSONObject("details").getString("value");
                                    Log.e("==slug", "Duplicate slug: " + duplicateSlug);

                                    // Generate a fresh short id (randomized) and form a new slug using the same base
                                    String freshShort = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
                                    if (freshShort.length() > 12)
                                        freshShort = freshShort.substring(0, 12);
                                    String newSlug = "";
                                    newSlug = Slugs.uniqueSlug(baseSlug, freshShort);
                                    Log.e("==slug", "Duplicate slug returned by server. Retrying with new slug: " + slug);


                                    // Retry the API call with new slug
                                    retryCreateForum(title, description, category, verified, newSlug, 1, dialog);
                                    return;
                                }
                            }

                            Toast.makeText(getActivity(), "Failed: " + response.code(), Toast.LENGTH_LONG).show();
                        } catch (Exception e) {
                            e.printStackTrace();
                            Toast.makeText(getActivity(), "Error parsing failure", Toast.LENGTH_LONG).show();
                        }
                        return;
                    }

                    // ✅ Success flow
                    CreateForumResponse resp = response.body();
                    if (resp == null || !resp.isSuccess() || resp.getData() == null || resp.getData().getForum() == null) {
                        Toast.makeText(getActivity(), "Unexpected response", Toast.LENGTH_LONG).show();
                        return;
                    }
                    Log.e("==lag", "token: " + resp.getData().getForum().getId());

                    CreateForumResponse.Forum f = resp.getData().getForum();
// DEBUG / instrumentation
                    Log.e("ForumsFragment", ">>> CREATE SUCCESS - server forum object:");
                    Log.e("ForumsFragment", " id         = " + f.getId());
                    Log.e("ForumsFragment", " name       = " + f.getName());
                    Log.e("ForumsFragment", " slug(server)= " + f.getSlug());
                    Log.e("ForumsFragment", " createdAt  = '" + f.getCreatedAt() + "'");
                    Log.e("ForumsFragment", " category(var)= " + category);
                    Log.e("ForumsFragment", " currentFilter= " + currentFilter);

// if createdAt null -> show
                    if (f.getCreatedAt() == null) {
                        Log.w("ForumsFragment", " server createdAt IS NULL");
                    }
                    // Map server forum -> UI model
                    String meta = f.getFollowersCount() + " followers • " + f.getMembersCount() + " members";

                    ForumModel uiModel = new ForumModel(
                            f.getId(),
                            f.getName(),
                            meta,
                            "Just now",
                            f.isVerified() ? "Verified" : "New",
                            category,
                            R.drawable.forum1,
                            permission, // ✅ save it here
                            f.getOwner(),
                            slug,
                            f.getCreatedAt()
                    );
// Mark newly-created local forum so UI shows "New" immediately
                    uiModel.setLocalNew(true);
                    addForumToList(uiModel);
                    Utills.hideLoadingDialog();
                    Toast.makeText(getActivity(), "Forum created", Toast.LENGTH_SHORT).show();
                    dialog.dismiss();
                }


                @Override
                public void onFailure(Call<CreateForumResponse> call, Throwable t) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            createBtn.setEnabled(true);
                            Utills.hideLoadingDialog();
                            dialog.dismiss();
                            Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();

                        });
                    }
                }
            });

        });
    }

    private void addForumToList(ForumModel newForum) {
        // Add to top of allForums list
        allForums.add(0, newForum);

        // persist
        saveForums();

        // Re-filter using current search text to produce filtered list & adapter
        String currentQuery = (searchBar != null) ? searchBar.getText().toString() : "";
        performLiveSearch(currentQuery);

        // Ensure layout manager exists and scroll to top
        if (forumRecycler.getLayoutManager() == null) {
            forumRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        }
        forumRecycler.scrollToPosition(0);

        // If adapter exists, notify item inserted (faster than full notify)
        RecyclerView.Adapter adapter = forumRecycler.getAdapter();
        if (adapter != null) {
            try {
                adapter.notifyItemInserted(0);
            } catch (Exception e) {
                adapter.notifyDataSetChanged();
            }
        }
    }

    private void retryCreateForum(String title, String description, String category, boolean verified, String newSlug, int attempt, AlertDialog dialog) {
        if (attempt > 5) { // Limit retries to avoid infinite loop
            if (isAdded()) {
                requireActivity().runOnUiThread(() -> {
                    Utills.hideLoadingDialog();
                    Toast.makeText(getActivity(), "Failed to create forum after multiple attempts", Toast.LENGTH_LONG).show();
                    if (dialog != null && dialog.isShowing()) dialog.dismiss();
                });
            } else {
                Utills.hideLoadingDialog();
                if (dialog != null && dialog.isShowing()) dialog.dismiss();
            }
            Log.e("==slug", "Exceeded max retry attempts for slug: " + newSlug);
            return;
        }
        String accessToken = prefs.getString("accessToken", null);

        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        CreateForumRequest body = new CreateForumRequest(
                title,
                newSlug,
                description,
                verified,
                permission
        );
        Log.e("==lag", "Attempt " + attempt + " with slug: " + newSlug);

        apiService.createForum("Bearer " + accessToken, body).enqueue(new Callback<CreateForumResponse>() {
            @Override
            public void onResponse(Call<CreateForumResponse> call, Response<CreateForumResponse> response) {
                Utills.hideLoadingDialog();
                if (!response.isSuccessful()) {
                    try {
                        String errorBody = response.errorBody() != null ? response.errorBody().string() : "";
                        if (!errorBody.isEmpty()) {
                            JSONObject obj = new JSONObject(errorBody);
                            if (obj.has("error")) {
                                JSONObject error = obj.getJSONObject("error");
                                String code = error.optString("code", "");
                                // Duplicate slug -> retry with new suffix
                                if ("DUPLICATE_ERROR".equals(code) &&
                                        error.getJSONObject("details").optString("field", "").equals("slug")) {

                                    String duplicateSlug = error.getJSONObject("details").optString("value", newSlug);
                                    // Extract base (strip trailing -suffix if present)
                                    String base = duplicateSlug.replaceAll("-[a-z0-9]{1,}$", "");
                                    String freshShort = UUID.randomUUID().toString()
                                            .replaceAll("[^a-zA-Z0-9]", "")
                                            .toLowerCase();
                                    if (freshShort.length() > 12) freshShort = freshShort.substring(0, 12);

                                    String nextSlug = Slugs.uniqueSlug(base, freshShort);
                                    Log.e("==slug", "Duplicate detected. Retrying with newSlug: " + nextSlug);
                                    // Recursive retry with incremented attempt
                                    retryCreateForum(title, description, category, verified, nextSlug, attempt + 1, dialog);
                                    return;
                                }
                            }
                        }
                    } catch (Exception e) {
                        Log.e("==slug", "Error parsing error body: " + e.getMessage());
                    }

                    // Non-retryable failure: show message and dismiss
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            Toast.makeText(getActivity(), "Failed to create forum: " + response.code(), Toast.LENGTH_LONG).show();
                            if (dialog != null && dialog.isShowing()) dialog.dismiss();
                        });
                    } else {
                        if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    }
                    return;
                }
                // ✅ Success
                CreateForumResponse resp = response.body();
                if (resp == null || !resp.isSuccess() || resp.getData() == null || resp.getData().getForum() == null) {
                    if (isAdded()) {
                        requireActivity().runOnUiThread(() -> {
                            if (dialog != null && dialog.isShowing()) dialog.dismiss();
                            Toast.makeText(getActivity(), "Unexpected response from server", Toast.LENGTH_LONG).show();
                        });
                    }else {
                        if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    }
                    return;
                }
                Log.e("==lag", "token: " + resp.getData().getForum().getId());

                CreateForumResponse.Forum f = resp.getData().getForum();
                // DEBUG / instrumentation
                Log.e("ForumsFragment", ">>> CREATE SUCCESS - server forum object:");
                Log.e("ForumsFragment", " id         = " + f.getId());
                Log.e("ForumsFragment", " name       = " + f.getName());
                Log.e("ForumsFragment", " slug(server)= " + f.getSlug());
                Log.e("ForumsFragment", " createdAt  = '" + f.getCreatedAt() + "'");
                Log.e("ForumsFragment", " category(var)= " + category);
                Log.e("ForumsFragment", " currentFilter= " + currentFilter);

// if createdAt null -> show
                if (f.getCreatedAt() == null) {
                    Log.w("ForumsFragment", " server createdAt IS NULL");
                }
                String meta = f.getFollowersCount() + " followers • " + f.getMembersCount() + " members";

                // Build UI model using returned data (use server slug if available)
                String serverSlug = (f.getSlug() != null && !f.getSlug().isEmpty()) ? f.getSlug() : newSlug;

                ForumModel uiModel = new ForumModel(
                        f.getId(),
                        f.getName(),
                        meta,
                        "Just now",
                        f.isVerified() ? "Verified" : "New",
                        category,
                        R.drawable.forum1,
                        f.getPostPermission(),
                        f.getOwner(),
                        serverSlug,
                        f.getCreatedAt()
                );
                // Mark newly-created local forum so UI shows "New" immediately
                uiModel.setLocalNew(true);
                // Add to list & update UI — but ensure we only add if not already present
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        if (!forumExists(uiModel)) {
                            addForumToList(uiModel); // add exactly once on main thread
                            Toast.makeText(getActivity(), "Forum created", Toast.LENGTH_SHORT).show();
                        } else {
                            Log.w("ForumsFragmentBottom", "Forum already exists locally, skipping add: " + uiModel.getId());
                        }
                        Utills.hideLoadingDialog();
                        if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    });
                } else {
                    // Fragment not attached — still persist but avoid UI operations
                    if (!forumExists(uiModel)) {
                        addForumToList(uiModel);
                        Log.i("ForumsFragmentBottom", "Forum persisted while fragment detached: " + uiModel.getId());
                    } else {
                        Log.w("ForumsFragmentBottom", "Forum already exists while detached: " + uiModel.getId());
                    }
                    Utills.hideLoadingDialog();
                    if (dialog != null && dialog.isShowing()) dialog.dismiss();
                }
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<CreateForumResponse> call, Throwable t) {
                Utills.hideLoadingDialog();
                Log.e("==lag", "Retry create forum failed: " + t.getMessage());
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getActivity(), "Network error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                        if (dialog != null && dialog.isShowing()) dialog.dismiss();
                    });
                } else {
                    if (dialog != null && dialog.isShowing()) dialog.dismiss();
                }
            }
        });
    }

    private void saveForums() {
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(allForums);
        editor.putString("forums_list", json);
        editor.apply();
    }

  /*  // --- small helper ---
    // --- small helper ---
    // --- small helper ---
    private static class Slugify {
        static String from(String title, Context context) {
            if (title == null) return "";

            // Clean title: only letters/numbers, spaces become underscores
            String baseSlug = title.toLowerCase()
                    .replaceAll("[^a-z0-9\\s]", "")  // keep only letters, numbers, spaces
                    .trim()
                    .replaceAll("\\s+", "_");       // spaces -> underscore

            if (baseSlug.isEmpty()) {
                baseSlug = "forum";
            }

            // Get device-specific UUID (ANDROID_ID is stable per device)
            String deviceUuid = android.provider.Settings.Secure.getString(
                    context.getContentResolver(),
                    android.provider.Settings.Secure.ANDROID_ID
            );

            if (deviceUuid == null || deviceUuid.isEmpty()) {
                deviceUuid = java.util.UUID.randomUUID().toString().replace("-", "");
            }

            return baseSlug + "_" + deviceUuid;
        }
    }


    private String incrementSlug(String slug) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)$");
        java.util.regex.Matcher matcher = pattern.matcher(slug);

        if (matcher.find()) {
            // Extract number at the end
            int num = Integer.parseInt(matcher.group(1));
            return slug.substring(0, matcher.start(1)) + (num + 1);
        } else {
            // If no number, append 1
            return slug + "1";
        }
    }*/
  private boolean forumExists(ForumModel candidate) {
      if (candidate == null) return false;
      String candId = candidate.getId() == null ? "" : candidate.getId().toLowerCase(Locale.ROOT);
      String candSlug = candidate.getSlug() == null ? "" : candidate.getSlug().toLowerCase(Locale.ROOT);
      for (ForumModel fm : allForums) {
          String existingId = fm.getId() == null ? "" : fm.getId().toLowerCase(Locale.ROOT);
          String existingSlug = fm.getSlug() == null ? "" : fm.getSlug().toLowerCase(Locale.ROOT);
          if (!existingId.isEmpty() && !candId.isEmpty() && existingId.equals(candId)) return true;
          if (!existingSlug.isEmpty() && !candSlug.isEmpty() && existingSlug.equals(candSlug)) return true;
      }
      return false;
  }
    public static final class Slugs {
        // Build base slug matching backend rules
        public static String makeSlug(String raw) {
            if (raw == null) raw = "";
            // 1) remove accents
            String s = Normalizer.normalize(raw, Normalizer.Form.NFD);
            s = s.replaceAll("\\p{M}", ""); // remove diacritics
            s = s.toLowerCase(Locale.ROOT);
            // 2) spaces/underscores -> hyphen
            s = s.replaceAll("[\\s_]+", "-");
            // 3) allow only a-z0-9-
            s = s.replaceAll("[^a-z0-9-]", "");
            // 4) collapse multiple hyphens, trim ends
            s = s.replaceAll("-{2,}", "-").replaceAll("^-|-$", "");
            // 5) enforce length 3..64
            if (s.length() < 3) s = (s + "000").substring(0, 3);
            if (s.length() > 64) s = s.substring(0, 64);
            return s;
        }

        // Attach a short suffix derived from uuid (kept alphanumeric lowercase, up to 6 chars)
        public static String uniqueSlug(String base, String uuid) {
            String suffix = (uuid == null ? "" : uuid.replaceAll("[^a-zA-Z0-9]", "").toLowerCase());
            suffix = suffix.length() >= 6 ? suffix.substring(0, 6) : suffix;
            if (suffix.isEmpty()) return base;
            String sep = "-";
            int maxBase = Math.max(0, 64 - (sep.length() + suffix.length()));
            String trimmed = base.length() > maxBase ? base.substring(0, maxBase) : base;
            return trimmed + sep + suffix;
        }

        // Helper: produce a short stable-ish id: prefer androidId else random
        public static String shortIdFromAndroidIdOrRandom(Context ctx) {
            String androidId = null;
            try {
                androidId = Settings.Secure.getString(
                        ctx.getContentResolver(), Settings.Secure.ANDROID_ID);
            } catch (Exception ignored) {
            }
            String src = (androidId == null || androidId.isEmpty())
                    ? UUID.randomUUID().toString()
                    : androidId;
            String clean = src.replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            if (clean.length() == 0)
                clean = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "").toLowerCase();
            return clean.length() > 12 ? clean.substring(0, 12) : clean;
        }
    }

    private void showJoinForumDialog() {
        // Prevent multiple dialogs
        if (currentDialog2 != null && currentDialog2.isShowing()) return;

        AlertDialog.Builder builder = new AlertDialog.Builder(requireActivity(), R.style.CustomDialog);
        LayoutInflater inflater = getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.dialog_join_forum, null);
        builder.setView(dialogView);

        EditText inputSlug = dialogView.findViewById(R.id.input_join_slug);
        TextView errorText = dialogView.findViewById(R.id.join_error_text);
        TextView join_dialog_title = dialogView.findViewById(R.id.join_dialog_title);
        TextView joinBtn = dialogView.findViewById(R.id.btn_join_confirm);
        TextView cancelBtn = dialogView.findViewById(R.id.btn_join_cancel);
        ProgressBar progress = dialogView.findViewById(R.id.join_progress);

        currentDialog2 = builder.create();
        currentDialog2.setCanceledOnTouchOutside(true);

        Utills.GradientText(join_dialog_title);
        cancelBtn.setOnClickListener(v -> {
            if (currentDialog2 != null && currentDialog2.isShowing()) currentDialog2.dismiss();
        });

        joinBtn.setOnClickListener(v -> {
            String slug = inputSlug.getText().toString().trim();
            if (slug.isEmpty()) {
                errorText.setText("Please enter a forum slug or invite code.");
                errorText.setVisibility(View.VISIBLE);
                return;
            }
            Utills.showLoadingDialog(getActivity());

            // ---------- DUPLICATE CHECK (before calling API) ----------
            // Normalize input for comparison
            String normalizedInput = slug.toLowerCase(Locale.ROOT).trim();

            boolean alreadyExists = false;
            for (ForumModel fm : allForums) {
                // Adapt these getters if your ForumModel uses different names.
                String existingId = fm.getId() == null ? "" : fm.getId().toLowerCase(Locale.ROOT);
                String existingCategory = fm.getCategory() == null ? "" : fm.getCategory().toLowerCase(Locale.ROOT);
                String existingTitle = fm.getTitle() == null ? "" : fm.getTitle().toLowerCase(Locale.ROOT);

                if (existingId.equals(normalizedInput)
                        || existingCategory.equals(normalizedInput)
                        || existingTitle.equals(normalizedInput)) {
                    alreadyExists = true;
                    break;
                }
            }

            if (alreadyExists) {
                Utills.hideLoadingDialog();
                joinBtn.setEnabled(true);
                progress.setVisibility(View.GONE);
                errorText.setText("You already have this forum in your list. please join new forum.");
                errorText.setVisibility(View.VISIBLE);
                return;
            }
            // ---------- end duplicate check ----------

            // Hide error, show progress
            errorText.setVisibility(View.GONE);
            joinBtn.setEnabled(false);
            progress.setVisibility(View.VISIBLE);
            String accessToken = prefs.getString("accessToken", null);
            Log.e("==lag", "token : " + accessToken);
            // ✅ Call real API
            ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
            Call<ForumBySlugResponse> call = apiService.getForumBySlug("Bearer " + accessToken, slug);

            call.enqueue(new Callback<ForumBySlugResponse>() {
                @Override
                public void onResponse(Call<ForumBySlugResponse> call, Response<ForumBySlugResponse> response) {
                    Utills.hideLoadingDialog();
                    if (currentDialog2 != null && currentDialog2.isShowing())
                        currentDialog2.dismiss();
                    if (!isAdded()) return;

                    joinBtn.setEnabled(true);
                    progress.setVisibility(View.GONE);

                    if (!response.isSuccessful()) {
                        Utills.hideLoadingDialog();
                        if (currentDialog2 != null && currentDialog2.isShowing())
                            currentDialog2.dismiss();
                        String msg = "Failed: " + response.code();
                        try {
                            if (response.errorBody() != null) {
                                msg = response.errorBody().string();
                            }
                        } catch (Exception ignored) {
                        }
                        errorText.setText("Error: " + msg);
                        errorText.setVisibility(View.VISIBLE);
                        return;
                    }

                    ForumBySlugResponse body = response.body();
                    if (body == null || !body.isSuccess() || body.getData() == null || body.getData().getForum() == null) {
                        errorText.setText("Forum not found or unexpected response.");
                        errorText.setVisibility(View.VISIBLE);
                        return;
                    }

                    ForumBySlugResponse.Data.Forum f = body.getData().getForum();

                    // Build meta string
                    String meta = f.getFollowersCount() + " followers • " + f.getMembersCount() + " members";

                    // Map server forum -> UI model
                    ForumModel uiModel = new ForumModel(
                            f.getId(),
                            f.getName(),
                            meta,
                            "Just now",
                            f.isVerified() ? "Verified" : "New",
                            f.getSlug(),              // using slug as category fallback
                            R.drawable.forum1,        // placeholder icon
                            f.getPostPermission(),
                            f.getOwner(),f.getSlug(),
                            f.getCreatedAt()
                    );
// Mark newly-created local forum so UI shows "New" immediately
                    uiModel.setLocalNew(true);
                    // Add to local list + persist
                    addForumToList(uiModel);

                    Toast.makeText(getActivity(), "Joined forum: " + f.getName(), Toast.LENGTH_SHORT).show();

                    // Close dialog
                    if (currentDialog2 != null && currentDialog2.isShowing())
                        currentDialog2.dismiss();
                }

                @Override
                public void onFailure(Call<ForumBySlugResponse> call, Throwable t) {
                    Utills.hideLoadingDialog();
                    if (currentDialog2 != null && currentDialog2.isShowing())
                        currentDialog2.dismiss();
                    if (!isAdded()) return;

                    joinBtn.setEnabled(true);
                    progress.setVisibility(View.GONE);
                    errorText.setText("Network error: " + t.getMessage());
                    errorText.setVisibility(View.VISIBLE);
                }
            });
        });

        currentDialog2.setOnDismissListener(d -> currentDialog2 = null);

        // show then safely adjust window
        if (!isAdded()) return;            // fragment not attached -> avoid crashes
        currentDialog2.show();

        try {
            if (currentDialog2.getWindow() != null) {
                int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.92);
                currentDialog2.getWindow().setLayout(width, ViewGroup.LayoutParams.WRAP_CONTENT);
                currentDialog2.getWindow().setBackgroundDrawableResource(android.R.color.transparent);
                currentDialog2.getWindow().setGravity(Gravity.CENTER);
            }
        } catch (Exception e) {
            Log.w("ForumsFragmentBottom", "Failed to resize dialog window: " + e.getMessage());
        }

    }
}


