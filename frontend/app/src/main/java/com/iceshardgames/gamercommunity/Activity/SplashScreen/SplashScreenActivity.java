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

    private ExoPlayer player;
    private String videoUrl;
    ActivitySplashScreenBinding binding;
    private Handler handler = new Handler();
    private boolean isMinimumTimeElapsed = false;
    private boolean isVideoEnded = false;
    private boolean isTransitionInitiated = false; // To prevent multiple transitions

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivitySplashScreenBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        View decorView = getWindow().getDecorView();
        int uiOptions = View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY;
        decorView.setSystemUiVisibility(uiOptions);

//        String localVideoPath = "android.resource://" + getPackageName() + "/" + R.raw.vidsplash;

        LottieFile();
//        initializePlayer(localVideoPath);
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

    /* private void initializePlayer(String videoPath) { // Now accepts videoPath as a parameter
        if (player == null) {
            player = new ExoPlayer.Builder(this).build();
            binding.fullscreenVideoPlayerView.setPlayer(player);
            player.setVolume(1f); // Set volume to audible level for full screen
            player.setRepeatMode(Player.REPEAT_MODE_OFF); // Don't loop by default in full screen

            // Add a listener for playback errors
            player.addListener(new Player.Listener() {
                @Override
                public void onPlayerError(PlaybackException error) {
//                    Toast.makeText(SplashScreenActivity.this, "Playback error: " + error.getMessage(), Toast.LENGTH_LONG).show();
                    // If video fails, still attempt to transition after minimum time
                    isVideoEnded = true; // Treat error as video ending for transition purposes
                    attemptTransitionToNextActivity();
                }

                @Override
                public void onPlaybackStateChanged(int playbackState) {
                    if (playbackState == Player.STATE_ENDED) {
                        isVideoEnded = true;
                        attemptTransitionToNextActivity();
                    }
                }
            });
        }

        MediaItem mediaItem = MediaItem.fromUri(Uri.parse(videoPath)); // Use the passed videoPath
        player.setMediaItem(mediaItem);
        player.prepare();
        player.play(); // Start playback automatically
    }

    private void attemptTransitionToNextActivity() {
        // Only transition if both conditions are met and it hasn't been initiated yet
        if (isMinimumTimeElapsed && isVideoEnded && !isTransitionInitiated) {
            isTransitionInitiated = true; // Set flag to prevent multiple calls
            Intent intent = new Intent(SplashScreenActivity.this, StartScreenActivity.class); // Replace MainActivity.class with your actual main activity
            startActivity(intent);
            finish(); // Finish SplashScreenActivity so user can't go back to it
        }
    }

    private void releasePlayer() {
        if (player != null) {
            player.release();
            player = null;
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (player != null && !player.isPlaying()) {
            player.play(); // Resume playback if paused
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (player != null) {
            player.pause(); // Pause playback when activity goes to background
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        releasePlayer(); // Release player when activity is stopped
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        releasePlayer(); // Ensure player is released when activity is destroyed
        handler.removeCallbacksAndMessages(null); // Remove any pending callbacks

    }*/
}