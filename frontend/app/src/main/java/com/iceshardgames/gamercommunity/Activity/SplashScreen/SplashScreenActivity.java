package com.iceshardgames.gamercommunity.Activity.SplashScreen;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.os.Handler; // Import Handler
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.iceshardgames.gamercommunity.Activity.StartScreen.StartScreenActivity;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.databinding.ActivitySplashScreenBinding;

public class SplashScreenActivity extends AppCompatActivity {
    ActivitySplashScreenBinding binding;
    private Handler handler = new Handler();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN;
        decorView.setSystemUiVisibility(uiOptions);
        LottieFile();
    }

   private void LottieFile() {
        binding.lottieAnimationView.setAnimation(R.raw.vidspl);
        binding.lottieAnimationView.playAnimation();
        binding.lottieAnimationView.loop(true);
        handler.postDelayed(() -> {
            Intent intent = new Intent(SplashScreenActivity.this, StartScreenActivity.class); // Replace MainActivity.class with your actual main activity
            startActivity(intent);
            finish();
        }, 5000);

    }
}