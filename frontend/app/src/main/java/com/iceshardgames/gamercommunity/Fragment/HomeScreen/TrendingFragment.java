package com.iceshardgames.gamercommunity.Fragment.HomeScreen;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Adapter.GameAdapter;
import com.iceshardgames.gamercommunity.Adapter.NewsAdapter;
import com.iceshardgames.gamercommunity.Model.GameModel;
import com.iceshardgames.gamercommunity.Model.NewsModel;
import com.iceshardgames.gamercommunity.R;

import java.util.ArrayList;

public class TrendingFragment extends Fragment {
    private RecyclerView recyclerTrendingGames, recyclerNews;
    private GameAdapter gameAdapter;
    private NewsAdapter newsAdapter;

    private ArrayList<GameModel> gameList;
    private ArrayList<NewsModel> newsList;

    public TrendingFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_trending, container, false);

        recyclerTrendingGames = view.findViewById(R.id.recyclerTrending);
        recyclerNews = view.findViewById(R.id.recyclerNews);

        // Setup Game RecyclerView
        recyclerTrendingGames.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.VERTICAL, false));
        gameList = new ArrayList<>();
        loadDummyGames();
        gameAdapter = new GameAdapter(getContext(), gameList);
        recyclerTrendingGames.setAdapter(gameAdapter);

        // Setup News RecyclerView
        recyclerNews.setLayoutManager(new LinearLayoutManager(getContext()));
        newsList = new ArrayList<>();
        loadDummyNews();
        newsAdapter = new NewsAdapter(getContext(), newsList);
        recyclerNews.setAdapter(newsAdapter);
        return view;
    }

    private void loadDummyGames() {
        gameList.add(new GameModel("Half-Life: Alyx", "Action", R.drawable.img1, 4.9,12));
        gameList.add(new GameModel("Beat Saber", "Rhythm", R.drawable.img2, 4.8,8));
        gameList.add(new GameModel("Boneworks", "Physics", R.drawable.img1, 4.7,15));
        gameList.add(new GameModel("Pavlov VR", "Shooter", R.drawable.img2, 4.6,22));
        gameList.add(new GameModel("Superhot VR", "Action", R.drawable.img1, 4.8,5));
        gameList.add(new GameModel("VRChat", "Social", R.drawable.img2, 4.4,18));
    }

    private void loadDummyNews() {
        newsList.add(new NewsModel("Meta", "update", "Quest 3 gets major update with hand tracking 2.0", "1h ago"));
        newsList.add(new NewsModel("Valve", "beta", "Steam VR 2.0 beta now available", "3h ago"));
        newsList.add(new NewsModel("Sony", "announcement", "PSVR2 exclusive games announced", "5h ago"));
    }
}
