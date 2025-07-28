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
import com.iceshardgames.gamercommunity.Adapter.StoryAdapter;
import com.iceshardgames.gamercommunity.Model.DiscussionModel;
import com.iceshardgames.gamercommunity.Model.NewsModel;
import com.iceshardgames.gamercommunity.Model.StoryModel;
import com.iceshardgames.gamercommunity.R;

import java.util.ArrayList;
import java.util.List;

public class GamesFragment extends Fragment {
    private NewsAdapter newsAdapter;
    private RecyclerView recyclerNews;
    private ArrayList<NewsModel> newsList;


    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_games, container, false);

        recyclerNews = view.findViewById(R.id.recyclerNewsForGames);

        RecyclerView storyRecyclerView = view.findViewById(R.id.storyRecyclerView);
        storyRecyclerView.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        List<StoryModel> stories = new ArrayList<>();
        stories.add(new StoryModel(R.drawable.attrects, "You"));
        stories.add(new StoryModel(R.drawable.img_slider1, "Ayush"));
        stories.add(new StoryModel(R.drawable.img_slider3, "Nishant"));
        stories.add(new StoryModel(R.drawable.img_slider4, "Suhani"));
        stories.add(new StoryModel(R.drawable.img_slider5, "Vinit"));
        stories.add(new StoryModel(R.drawable.img_slider6, "Aryan"));
        stories.add(new StoryModel(R.drawable.img_slider7, "Yash"));
        stories.add(new StoryModel(R.drawable.img_slider8, "Rishav"));
        stories.add(new StoryModel(R.drawable.img_slider9, "Trusha"));
        stories.add(new StoryModel(R.drawable.img_slider10, "Mehul"));
        stories.add(new StoryModel(R.drawable.img_slider11, "Sattu"));
// Add more...

        StoryAdapter adapter = new StoryAdapter(stories, getContext());
        storyRecyclerView.setAdapter(adapter);


        // Setup News RecyclerView
        recyclerNews.setLayoutManager(new LinearLayoutManager(getContext()));
        newsList = new ArrayList<>();
        loadDummyNews();
        newsAdapter = new NewsAdapter(getContext(), newsList);
        recyclerNews.setAdapter(newsAdapter);

        return view;
    }


    private void loadDummyNews() {
        newsList.add(new NewsModel("Meta", "update", "Quest 3 gets major update with hand tracking 2.0", "1h ago"));
        newsList.add(new NewsModel("Valve", "beta", "Steam VR 2.0 beta now available", "3h ago"));
        newsList.add(new NewsModel("Sony", "announcement", "PSVR2 exclusive games announced", "5h ago"));
    }
}