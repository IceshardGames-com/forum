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

import com.iceshardgames.gamercommunity.Activity.EsportsScreen.Adapter.MatchUpcomingAdapter;
import com.iceshardgames.gamercommunity.Activity.EsportsScreen.Model.EsportsMatch;
import com.iceshardgames.gamercommunity.R;

import java.util.ArrayList;
import java.util.List;

public class UpcomingTabFragment extends Fragment {

    private RecyclerView recyclerView;
    private MatchUpcomingAdapter adapter;
    private List<EsportsMatch> matchList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_upcoming, container, false);
        recyclerView = view.findViewById(R.id.upcomingRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        matchList = getUpcomingMatches();
        adapter = new MatchUpcomingAdapter(getContext(),matchList);
        recyclerView.setAdapter(adapter);
        return view;
    }

    private List<EsportsMatch> getUpcomingMatches() {
        List<EsportsMatch> list = new ArrayList<>();
        list.add(new EsportsMatch("VR FPS Championship", "Elite Squad", "Ghost Operators", "Upcoming", "Starts in 1 Day", "$75,000", "Tomorrow 3PM", 0));
        list.add(new EsportsMatch("VR Racing League", "Speed Demons", "Velocity Racers", "Upcoming", "Starts in 2 Days", "$40,000", "This Weekend", 0));
        list.add(new EsportsMatch("Horror VR Survival", "Ghost Hunters", "Phantom Squad", "Upcoming", "Starts in 1 Week", "$30,000", "Next Week", 0));
        return list;
    }
}
