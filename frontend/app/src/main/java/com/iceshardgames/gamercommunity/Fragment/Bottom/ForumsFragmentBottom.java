package com.iceshardgames.gamercommunity.Fragment.Bottom;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageView;
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
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;

import org.json.JSONObject;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forums_bottom, container, false);

        Utills.GradientText(view.findViewById(R.id.title));


        ImageView btnCreateForum = view.findViewById(R.id.fabAddForum);
        btnCreateForum.setOnClickListener(v -> {
            showCreateForumDialog();
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

    private void loadForums() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String json = prefs.getString("forums_list", null);

        if (json != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<ForumModel>>() {
            }.getType();
            allForums = gson.fromJson(json, type);
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
            Utills.showLoadingDialog(getActivity());
            String title = titleInput.getText().toString().trim();
            String description = descInput.getText().toString().trim();
            String category = categorySpinner.getSelectedItem().toString();
            boolean isPrivate = privateCheckbox.isChecked();

            if (title.isEmpty() || description.isEmpty()) {
                Toast.makeText(getActivity(), "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
            String accessToken = prefs.getString("accessToken", null);
            Log.e("==pass", "token : " + accessToken);
            List<String> existingSlugs = new ArrayList<>();
            for (ForumModel f : allForums) {
                existingSlugs.add(Slugify.from(f.getTitle(), new ArrayList<>()));
            }

            // Build request body
            String slug = Slugify.from(title, existingSlugs);
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

                                    // Generate new slug
                                    String newSlug = incrementSlug(duplicateSlug);
                                    Log.e("==slug", "Retry with slug: " + newSlug);

                                    // Retry the API call with new slug
                                    retryCreateForum(title, description, category, verified, newSlug, 1,dialog);
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
                            permission // ✅ save it here
                    );

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
                            Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();

                        });
                    }
                }
            });

        });
    }

    private void addForumToList(ForumModel newForum) {
        allForums.add(0, newForum); // Add to top of allForums list
        saveForums(); // persist
        performLiveSearch(searchBar.getText().toString()); // Refresh with current search text
    }

    private void retryCreateForum(String title, String description, String category, boolean verified, String newSlug, int attempt, AlertDialog dialog) {
        if (attempt > 5) { // Limit retries to avoid infinite loop
            Toast.makeText(getActivity(), "Failed to create forum after multiple attempts", Toast.LENGTH_LONG).show();
            return;
        }
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);

        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        CreateForumRequest body = new CreateForumRequest(
                title,
                newSlug,
                description,
                verified,
                permission
        );

        apiService.createForum("Bearer " + accessToken, body).enqueue(new Callback<CreateForumResponse>() {
            @Override
            public void onResponse(Call<CreateForumResponse> call, Response<CreateForumResponse> response) {
                if (!response.isSuccessful()) {
                    try {
                        String errorBody = response.errorBody().string();
                        JSONObject obj = new JSONObject(errorBody);

                        if (obj.has("error")) {
                            JSONObject error = obj.getJSONObject("error");
                            if ("DUPLICATE_ERROR".equals(error.getString("code")) &&
                                    error.getJSONObject("details").getString("field").equals("slug")) {

                                String duplicateSlug = error.getJSONObject("details").getString("value");
                                String newSlug = incrementSlug(duplicateSlug);
                                Log.e("==slug", "Retry with slug: " + newSlug);

                                // Retry with increment
                                retryCreateForum(title, description, category, verified, newSlug, attempt + 1, dialog);
                                return;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                    Toast.makeText(getActivity(), "Failed: " + response.code(), Toast.LENGTH_LONG).show();
                    return;
                }
                // ✅ Success
                CreateForumResponse resp = response.body();
                if (resp == null || !resp.isSuccess() || resp.getData() == null || resp.getData().getForum() == null) {
                    Toast.makeText(getActivity(), "Unexpected response", Toast.LENGTH_LONG).show();
                    return;
                }
                Log.e("==lag", "token: " + resp.getData().getForum().getId());

                CreateForumResponse.Forum f = resp.getData().getForum();
                String meta = f.getFollowersCount() + " followers • " + f.getMembersCount() + " members";

                ForumModel uiModel = new ForumModel(f.getId(), f.getName(), meta, "Just now",
                        f.isVerified() ? "Verified" : "New", category, R.drawable.forum1, permission);

                addForumToList(uiModel);
                Utills.hideLoadingDialog();
                Toast.makeText(getActivity(), "Forum created", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            }

            @Override
            public void onFailure(Call<CreateForumResponse> call, Throwable t) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        Utills.hideLoadingDialog();
                        Toast.makeText(getActivity(), "Error: " + t.getMessage(), Toast.LENGTH_LONG).show();
                    });
                }
            }
        });
    }

    private void saveForums() {
        SharedPreferences prefs = requireContext().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();

        Gson gson = new Gson();
        String json = gson.toJson(allForums);
        editor.putString("forums_list", json);
        editor.apply();
    }

    // --- small helper ---
    // --- small helper ---
    private static class Slugify {
        static String from(String s, List<String> existingSlugs) {
            if (s == null) return "";

            // Step 1: Clean slug
            String baseSlug = s.toLowerCase()
                    .replaceAll("[^a-z0-9\\s-]", "")   // keep only letters, numbers, space, dash
                    .trim()
                    .replaceAll("\\s+", "-")          // spaces -> dash
                    .replaceAll("-{2,}", "-")         // collapse multiple dashes
                    .replaceAll("^[-_]+|[-_]+$", ""); // trim leading/trailing -/_

            if (baseSlug.isEmpty()) baseSlug = "forum";

            // Step 2: Ensure uniqueness
            String uniqueSlug = baseSlug;
            int counter = 1;
            while (existingSlugs.contains(uniqueSlug)) {
                uniqueSlug = baseSlug + counter;  // <-- ✅ no dash here
                counter++;
            }

            return uniqueSlug;
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
    }


}


