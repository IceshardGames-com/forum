package com.iceshardgames.gamercommunity.Activity.MainScreen;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.iceshardgames.gamercommunity.Adapter.SimilarGameAdapter;
import com.iceshardgames.gamercommunity.Adapter.ReviewAdapter;
import com.iceshardgames.gamercommunity.Adapter.ScreenshotAdapter;
import com.iceshardgames.gamercommunity.Model.Game;
import com.iceshardgames.gamercommunity.Model.ReviewModel;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;
import com.iceshardgames.gamercommunity.databinding.ActivityGameDetailBinding;

import java.util.ArrayList;

public class GameDetailActivity extends AppCompatActivity {

    ActivityGameDetailBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityGameDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Utills.GradientText(binding.header.screenTitleNav);
        Utills.GradientText(binding.gameTitle);
        Utills.GradientText(binding.abouttext);
        Utills.GradientText(binding.relesetext);
        Utills.GradientText(binding.fettext);
        Utills.GradientText(binding.sstext);
        Utills.GradientText(binding.revtext);
        Utills.GradientText(binding.simgametext);
        Utills.GradientText(binding.Downtext);
        binding.header.screenTitleNav.setText("Game Details");

        Intent intent = getIntent();
        binding.gameTitle.setText(intent.getStringExtra("title"));
        binding.genreText.setText(intent.getStringExtra("genre"));
        double rating = intent.getDoubleExtra("rating", 0);
        binding.ratingText.setText(String.valueOf(rating));
        binding.Download.setText(intent.getIntExtra("downloads", 0)+ " K Players");
        binding.headerImage.setImageResource(intent.getIntExtra("image", R.drawable.placeholder));


        // Screenshot Recycler
        binding.recyclerScreenshots.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerScreenshots.setAdapter(new ScreenshotAdapter(getDummyScreenshots()));

        // Reviews Recycler
        binding.recyclerReviews.setLayoutManager(new LinearLayoutManager(this));
        binding.recyclerReviews.setAdapter(new ReviewAdapter(getDummyReviews()));

        // Similar Games
        binding.recyclerSimilarGames.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        binding.recyclerSimilarGames.setAdapter(new SimilarGameAdapter(this, getDummyGames(), game -> {
            Toast.makeText(this, "Clicked: " + game.getTitle(), Toast.LENGTH_SHORT).show();
        }));
        binding.aboutText.setText("Beat Saber is a VR rhythm game where you slash the beats of adrenaline-pumping music as they fly towards you, surrounded by a futuristic world.");

       /* // Populate data
        binding.headerImage.setImageResource(R.drawable.img1); // replace with dynamic image
        binding.gameTitle.setText("Beat Saber");
        binding.developerName.setText("Beat Games");
        binding.genreText.setText("Rhythm, Music, VR");
        binding.ratingText.setText("4.8");
        binding.priceText.setText("$29.99");
        binding.aboutText.setText("Beat Saber is a VR rhythm game where you slash the beats of adrenaline-pumping music as they fly towards you, surrounded by a futuristic world.");
        binding.releaseDate.setText("May 21, 2019");*/
    }
    private ArrayList<Integer> getDummyScreenshots() {
        ArrayList<Integer> screenshots = new ArrayList<>();
        screenshots.add(R.drawable.img1);
        screenshots.add(R.drawable.img2);
        screenshots.add(R.drawable.img1);
        screenshots.add(R.drawable.img2);
        return screenshots;
    }

    private ArrayList<ReviewModel> getDummyReviews() {
        ArrayList<ReviewModel> list = new ArrayList<>();
        list.add(new ReviewModel("VRMaster", "Amazing VR Experience!", "This game completely blew me away. The immersion is incredible and the gameplay is perfectly designed for VR.", "2 days ago", 5));
        list.add(new ReviewModel("GameReviewer", "Great but has some issues", "Really enjoyable game with fantastic graphics. Some minor bugs but overall a great experience.", "1 week ago", 4));
        list.add(new ReviewModel("VRNewbie", "Perfect for beginners", "As someone new to VR, this game was the perfect introduction. Easy to learn but hard to master.", "2 weeks ago", 5));
        return list;
    }

    private ArrayList<Game> getDummyGames() {
        ArrayList<Game> games = new ArrayList<>();
        games.add(new Game("Boneworks", "Action", R.drawable.img1, 4.7, 10000));
        games.add(new Game("Superhot VR", "Shooter", R.drawable.img2, 4.8, 9000));
        games.add(new Game("Pavlov VR", "Shooter", R.drawable.img1, 4.6, 11000));
        return games;
    }
}