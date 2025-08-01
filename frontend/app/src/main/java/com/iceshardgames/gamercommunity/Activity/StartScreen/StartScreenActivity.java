package com.iceshardgames.gamercommunity.Activity.StartScreen;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.WindowInsetsController;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.iceshardgames.gamercommunity.Activity.LoginScreen.LoginScreenActivity;
import com.iceshardgames.gamercommunity.Activity.MainScreen.DashboardScreenActivity;
import com.iceshardgames.gamercommunity.Adapter.VideoAdapter;
import com.iceshardgames.gamercommunity.Model.VideoModel;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.databinding.ActivityStartScreenBinding;

import java.util.ArrayList;
import java.util.List;

public class StartScreenActivity extends AppCompatActivity {

    ActivityStartScreenBinding binding;
    private VideoAdapter videoAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityStartScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // --- FIX: Updated Immersive Mode Logic for API 30+ ---
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            getWindow().setDecorFitsSystemWindows(false);
            WindowInsetsController insetsController = getWindow().getInsetsController();
            if (insetsController != null) {
                insetsController.hide(WindowInsetsCompat.Type.systemBars());
                insetsController.setSystemBarsBehavior(WindowInsetsController.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE);
            }
        } else {
            // For older APIs (pre-API 30)
            View decorView = getWindow().getDecorView();
            int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
            decorView.setSystemUiVisibility(uiOptions);
        }
        // Apply window insets for padding (if not fully immersive or for system gestures)
        ViewCompat.setOnApplyWindowInsetsListener(binding.getRoot(), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Clicks();
    }

    private void Clicks() {
        // Set up RecyclerView with a GridLayoutManager (2 columns)
        StaggeredGridLayoutManager layoutManager = new StaggeredGridLayoutManager(3, StaggeredGridLayoutManager.VERTICAL);
        layoutManager.setGapStrategy(StaggeredGridLayoutManager.GAP_HANDLING_MOVE_ITEMS_BETWEEN_SPANS); // Helps prevent gaps
        binding.videoGridRecyclerView.setLayoutManager(layoutManager);
        binding.videoGridRecyclerView.setOnTouchListener((v, event) -> true); // Blocks touch scrolling

        // Create dummy video data with placeholder URLs
        List<VideoModel> videoList = generateDummyVideoData();

        // Initialize and set the adapter
        videoAdapter = new VideoAdapter(this, videoList);
        binding.videoGridRecyclerView.setAdapter(videoAdapter);


        binding.actionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SharedPreferences prefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
                boolean isLoggedIn = prefs.getBoolean("isLoggedIn", false);

                if (isLoggedIn) {
                    // Already logged in → go to Dashboard
                    Intent intent = new Intent(StartScreenActivity.this, DashboardScreenActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    // Not logged in → go to Login screen
                    Intent intent = new Intent(StartScreenActivity.this, LoginScreenActivity.class);
                    startActivity(intent);
                    finish();
                }
            }
        });
    }

    private List<VideoModel> generateDummyVideoData() {
        List<VideoModel> videos = new ArrayList<>();
//        String sampleVideoUrl = "https://www.learningcontainer.com/wp-content/uploads/2020/05/sample-mp4-file.mp4";
        String sampleVideoUrl = "android.resource://" + getPackageName() + "/" + R.raw.my_vr_game_clip;

        videos.add(new VideoModel("VR Space Odyssey", "android.resource://" + getPackageName() + "/" + R.raw.v1adventure));
        videos.add(new VideoModel("Cyberpunk City Tour VR", "android.resource://" + getPackageName() + "/" + R.raw.v2pubg));
        videos.add(new VideoModel("Ancient Ruins Exploration", "android.resource://" + getPackageName() + "/" + R.raw.v3apex_legends));
        videos.add(new VideoModel("Futuristic Racing League", "android.resource://" + getPackageName() + "/" + R.raw.v4fortnite));
        videos.add(new VideoModel("Deep Sea VR Dive", "android.resource://" + getPackageName() + "/" + R.raw.v5farcry));
        videos.add(new VideoModel("Deep Sea VR Dive5", "android.resource://" + getPackageName() + "/" + R.raw.v10cyberpunk2));
        videos.add(new VideoModel("Deep Sea VR Dive3", "android.resource://" + getPackageName() + "/" + R.raw.v8justcase));
        videos.add(new VideoModel("Deep Sea VR Dive2", "android.resource://" + getPackageName() + "/" + R.raw.v7mafiya));
        videos.add(new VideoModel("Deep Sea VR Dive4", "android.resource://" + getPackageName() + "/" + R.raw.v9wd_legion));
        videos.add(new VideoModel("Deep Sea VR Dive1", "android.resource://" + getPackageName() + "/" + R.raw.v6saintsrow));
        videos.add(new VideoModel("Deep Sea VR Dive6", "android.resource://" + getPackageName() + "/" + R.raw.v11cyberpunk));
        videos.add(new VideoModel("Deep Sea VR Dive7", "android.resource://" + getPackageName() + "/" + R.raw.v12gta5));
        // Add more as needed
        return videos;
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (binding != null && binding.videoGridRecyclerView != null && videoAdapter != null) {
            videoAdapter.notifyDataSetChanged(); // Force re-binding of visible items
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        binding = null;
    }

}