package com.iceshardgames.gamercommunity.Fragment.HomeScreen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Adapter.DiscussionAdapter;
import com.iceshardgames.gamercommunity.Adapter.NewsAdapter;
import com.iceshardgames.gamercommunity.Model.DiscussionModel;
import com.iceshardgames.gamercommunity.Model.NewsModel;
import com.iceshardgames.gamercommunity.R;

import java.util.ArrayList;
import java.util.List;

public class ForumsFragment extends Fragment {
    private RecyclerView discussionRecyclerView;
    private DiscussionAdapter discussionAdapter;
    private List<DiscussionModel> discussionList;

    private NewsAdapter newsAdapter;
    private RecyclerView recyclerNews;
    private ArrayList<NewsModel> newsList;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_forums, container, false);

        discussionRecyclerView = view.findViewById(R.id.discussionRecyclerView);
        discussionRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Set up adapter and data
        discussionList = getDummyDiscussions(); // or real API
        discussionAdapter = new DiscussionAdapter(discussionList);
        discussionRecyclerView.setAdapter(discussionAdapter);

        //news
        recyclerNews = view.findViewById(R.id.recyclerNewsForGames);
        // Setup News RecyclerView
        recyclerNews.setLayoutManager(new LinearLayoutManager(getContext()));
        newsList = new ArrayList<>();
        loadDummyNews();
        newsAdapter = new NewsAdapter(getContext(), newsList);
        recyclerNews.setAdapter(newsAdapter);
        return view;
    }

    private List<DiscussionModel> getDummyDiscussions() {
        List<DiscussionModel> list = new ArrayList<>();
        list.add(new DiscussionModel(
                "VR Update",
                "Meta Quest 3 just got a new hand-tracking update!",
                "Sapana V.",
                "48",
                "1h ago","156"
        ));

        list.add(new DiscussionModel(
                "Game Launch",
                "New Cyber Arena VR is launching next week 🔥",
                "VRDevTeam",
                "112",
                "3h ago","289"));

        list.add(new DiscussionModel(
                "Patch Notes",
                "Update 2.3.5: Fixed tracking bugs in PvP mode.",
                "IceShardGames",
                "36",
                "2d ago","412"));

        list.add(new DiscussionModel(
                "Esports",
                "VR Championship 2025: Who’s participating?",
                "Satyam VR",
                "85",
                "6h ago","89"));

        list.add(new DiscussionModel(
                "Bug Report",
                "Crashes when entering CyberSpace map.",
                "GamerX",
                "12",
                "45m ago","234"));

        list.add(new DiscussionModel(
                "Suggestions",
                "Add a new gun or powerups in Ranked mode?",
                "VRProGamer",
                "20",
                "5h ago","567"));

        list.add(new DiscussionModel(
                "Graphics",
                "Game is blurry on PSVR2. Any fix?",
                "Kriti_Dev",
                "9",
                "10h ago","777"));

        list.add(new DiscussionModel(
                "Feedback",
                "Loving the new neon UI theme!",
                "BetaTester42",
                "67",
                "1d ago","50"));
        return list;
    }
    private void loadDummyNews() {
        newsList.add(new NewsModel("Meta", "update", "Quest 3 gets major update with hand tracking 2.0", "1h ago"));
        newsList.add(new NewsModel("Valve", "beta", "Steam VR 2.0 beta now available", "3h ago"));
        newsList.add(new NewsModel("Sony", "announcement", "PSVR2 exclusive games announced", "5h ago"));
    }
}
