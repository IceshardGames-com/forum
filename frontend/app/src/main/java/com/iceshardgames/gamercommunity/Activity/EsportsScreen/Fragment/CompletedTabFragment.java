package com.iceshardgames.gamercommunity.Activity.EsportsScreen.Fragment;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Activity.EsportsScreen.Adapter.CompletedMatchesAdapter;
import com.iceshardgames.gamercommunity.Activity.EsportsScreen.Model.CompletedMatch;
import com.iceshardgames.gamercommunity.R;

import java.util.ArrayList;
import java.util.List;

public class CompletedTabFragment extends Fragment {

    private RecyclerView recyclerCompleted;
    private ImageView ivFilterCompleted;
    private CompletedMatchesAdapter adapter;
    private List<CompletedMatch> completedMatches;

    public CompletedTabFragment() {
        // Required empty constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_completed, container, false);
        recyclerCompleted = view.findViewById(R.id.rvCompletedMatches);
        ivFilterCompleted = view.findViewById(R.id.ivFilterCompleted);
        recyclerCompleted.setLayoutManager(new LinearLayoutManager(getContext()));


        ivFilterCompleted.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle("Filter Completed Matches");

            String[] filterOptions = {"Show All", "Won by Team Alpha", "Won by Team Bravo", "Matches After Aug 5, 2025"};
            builder.setItems(filterOptions, (dialog, which) -> {
                List<CompletedMatch> filteredList = new ArrayList<>();

                switch (which) {
                    case 0: // Show All
                        filteredList.addAll(completedMatches); // restore full list
                        break;
                    case 1: // Won by Team Alpha
                        for (CompletedMatch match : completedMatches) {
                            if (match.getTeam1Name().equalsIgnoreCase("Team Alpha") && match.getScore().startsWith("2")) {
                                filteredList.add(match);
                            }
                        }
                        break;
                    case 2: // Won by Team Bravo
                        for (CompletedMatch match : completedMatches) {
                            if (match.getTeam2Name().equalsIgnoreCase("Team Bravo") && match.getScore().endsWith("1")) {
                                filteredList.add(match);
                            }
                        }
                        break;
                    case 3: // Matches After Aug 5, 2025
                        for (CompletedMatch match : completedMatches) {
                            // Simple date check (string compare works since format is consistent)
                            if (match.getMatchDate().compareTo("Aug 5, 2025") > 0) {
                                filteredList.add(match);
                            }
                        }
                        break;
                }

                adapter.updateData(filteredList);
            });

            builder.show();
        });

        // Dummy Data
        completedMatches = new ArrayList<>();
        completedMatches.add(new CompletedMatch(
                "Team Alpha", R.drawable.profilepic,
                "Team Bravo", R.drawable.profilepic,
                "2 - 1", "Aug 8, 2025"));

        completedMatches.add(new CompletedMatch(
                "Team Omega", R.drawable.profilepic,
                "Team Sigma", R.drawable.profilepic,
                "0 - 3", "Aug 7, 2025"));

        completedMatches.add(new CompletedMatch(
                "Team Phoenix", R.drawable.profilepic,
                "Team Dragon", R.drawable.profilepic,
                "3 - 2", "Aug 6, 2025"));

        completedMatches.add(new CompletedMatch(
                "Team Knight", R.drawable.profilepic,
                "Team Rogue", R.drawable.profilepic,
                "1 - 0", "Aug 5, 2025"));

        completedMatches.add(new CompletedMatch(
                "Team Thunder", R.drawable.profilepic,
                "Team Storm", R.drawable.profilepic,
                "2 - 0", "Aug 4, 2025"));

        completedMatches.add(new CompletedMatch(
                "Team Blaze", R.drawable.profilepic,
                "Team Frost", R.drawable.profilepic,
                "0 - 2", "Aug 3, 2025"));

        completedMatches.add(new CompletedMatch(
                "Team Titan", R.drawable.profilepic,
                "Team Hydra", R.drawable.profilepic,
                "3 - 1", "Aug 2, 2025"));

        completedMatches.add(new CompletedMatch(
                "Team Nova", R.drawable.profilepic,
                "Team Eclipse", R.drawable.profilepic,
                "1 - 3", "Aug 1, 2025"));

        adapter = new CompletedMatchesAdapter(getContext(), completedMatches);
        recyclerCompleted.setAdapter(adapter);

        return view;
    }
}
