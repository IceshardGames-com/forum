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

import com.iceshardgames.gamercommunity.Activity.EsportsScreen.Adapter.MatchAdapter;
import com.iceshardgames.gamercommunity.Activity.EsportsScreen.Model.EsportsMatch;
import com.iceshardgames.gamercommunity.R;

import java.util.ArrayList;
import java.util.List;

public class LiveTabFragment extends Fragment {
    private RecyclerView recyclerView;
    private MatchAdapter adapter;
    private List<EsportsMatch> matchList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater,
                             @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_live, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewLiveMatches);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        matchList = getLiveMatches();
        adapter = new MatchAdapter(getContext(),matchList);
        recyclerView.setAdapter(adapter);
        return view;
    }

    private List<EsportsMatch> getLiveMatches() {
        List<EsportsMatch> list = new ArrayList<>();
        list.add(new EsportsMatch("VR Masters Championship", "Cyber Dragons", "Neon Wolves", "Live", "2-1", "$50,000", "Live Now", 12500));
        list.add(new EsportsMatch("Beat Saber World Cup", "RhythmKings", "SaberMasters", "Live", "1-1", "$25,000", "Live Now", 8700));
        return list;
    }
}

