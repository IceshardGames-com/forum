package com.iceshardgames.gamercommunity.Fragment;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
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

import com.iceshardgames.gamercommunity.Activity.LoginScreen.LoginScreenActivity;
import com.iceshardgames.gamercommunity.Activity.MainScreen.NotificationScreenActivity;
import com.iceshardgames.gamercommunity.Activity.ProfileScreen.SettingScreenActivity;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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
        prefs = requireContext().getSharedPreferences("ForumPrefs", Context.MODE_PRIVATE);
        String saved = prefs.getString("selected_filter", "All");
        currentFilter = saved.equalsIgnoreCase("All Games") ? "All" : saved;

        // Init Views
        searchBar = view.findViewById(R.id.searchBar);
        forumRecycler = view.findViewById(R.id.forumRecycler);

        Button btnAllGames = view.findViewById(R.id.btnAllGames);
        Button btnAction = view.findViewById(R.id.btnAction);
        Button btnRhythm = view.findViewById(R.id.btnRhythm);
        Button btnSimulation = view.findViewById(R.id.btnSimulation);
        Button btnShooter = view.findViewById(R.id.btnShooter);
        Button btnHorror = view.findViewById(R.id.btnHorror);

        filterButtons = Arrays.asList(btnAllGames, btnAction, btnRhythm, btnSimulation, btnShooter, btnHorror);

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
        btnAction.setOnClickListener(v -> setSelectedFilter(btnAction, filterButtons));
        btnRhythm.setOnClickListener(v -> setSelectedFilter(btnRhythm, filterButtons));
        btnSimulation.setOnClickListener(v -> setSelectedFilter(btnSimulation, filterButtons));
        btnShooter.setOnClickListener(v -> setSelectedFilter(btnShooter, filterButtons));
        btnHorror.setOnClickListener(v -> setSelectedFilter(btnHorror, filterButtons));

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

        setupForumList();
        // Ensure selection + filtering happens after layout is ready
        btnAllGames.post(() -> {
            setSelectedFilter(btnAllGames, filterButtons);
            filterAndDisplayForums("All");
        });
        filterAndDisplayForums(currentFilter);

        return view;
    }

    private void performLiveSearch(String query) {
        List<ForumModel> filtered = new ArrayList<>();
        for (ForumModel model : allForums) {
            if ((currentFilter.equals("All") || model.getCategory().equalsIgnoreCase(currentFilter)) &&
                    model.getTitle().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(model);
            }
        }

        ForumAdapter adapter = new ForumAdapter(getActivity(),filtered);
        forumRecycler.setAdapter(adapter);
    }


    private void setupForumList() {
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

    }


    private void filterAndDisplayForums(String category) {
        List<ForumModel> filtered = new ArrayList<>();
        for (ForumModel model : allForums) {
            if (category.equalsIgnoreCase("All") || model.getCategory().equalsIgnoreCase(category)) {
                filtered.add(model);
            }
        }

        ForumAdapter adapter = new ForumAdapter(getActivity(),filtered);
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

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        // Category options (you can load these dynamically too)
        String[] categories = {"Action", "Adventure", "Esports", "Casual", "RPG", "Simulation"};
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
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                charCount.setText(s.length() + "/500 characters");
            }
            @Override
            public void afterTextChanged(Editable s) {}
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

            // TODO: Save forum object to Firestore / Room / API
            ForumModel newForum = new ForumModel(title, description, "Just now", "New", category, R.drawable.forum1);

            // Optional: Add to RecyclerView list
            addForumToList(newForum);

            Toast.makeText(getActivity(), "Forum Created!", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
    }

    private void addForumToList(ForumModel newForum) {
        allForums.add(0, newForum); // Add to top of allForums list
        performLiveSearch(searchBar.getText().toString()); // Refresh with current search text
    }
}


