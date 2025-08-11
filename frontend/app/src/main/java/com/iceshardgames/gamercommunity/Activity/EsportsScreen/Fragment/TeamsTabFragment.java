package com.iceshardgames.gamercommunity.Activity.EsportsScreen.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Activity.EsportsScreen.Adapter.TeamsAdapter;
import com.iceshardgames.gamercommunity.Activity.EsportsScreen.Model.EsportsMatch;
import com.iceshardgames.gamercommunity.Activity.EsportsScreen.Model.Team;
import com.iceshardgames.gamercommunity.R;

import java.util.ArrayList;
import java.util.List;

public class TeamsTabFragment extends Fragment {

    private RecyclerView recyclerTeams;
    private TeamsAdapter adapter;
    private List<Team> teamList;

    public TeamsTabFragment() {
        // Required empty constructor
    }
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_teams, container, false);
        recyclerTeams = view.findViewById(R.id.recyclerTeams);
        teamList = getTeams();
        recyclerTeams.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new TeamsAdapter(getContext(), teamList);
        recyclerTeams.setAdapter(adapter);

        return view;
    }

    private List<Team> getTeams() {
        List<Team> myTeams = new ArrayList<>();

        myTeams.add(new Team(
                1,                     // Rank
                "Phoenix Flames",      // Team Name
                45,                    // Points
                15,                    // Wins
                3,                     // Losses
                R.drawable.img_slider10 // Team logo drawable
        ));

        myTeams.add(new Team(
                2,
                "Shadow Strikers",
                42,
                14,
                4,
                R.drawable.img_slider2
        ));

        myTeams.add(new Team(
                3,
                "Iron Titans",
                39,
                13,
                5,
                R.drawable.img_slider4
        ));

        myTeams.add(new Team(
                4,
                "Dragon Warriors",
                36,
                12,
                6,
                R.drawable.img_slider6
        ));

        myTeams.add(new Team(
                5,
                "Storm Riders",
                33,
                11,
                7,
                R.drawable.img_slider9
        ));
        return myTeams;
    }
}