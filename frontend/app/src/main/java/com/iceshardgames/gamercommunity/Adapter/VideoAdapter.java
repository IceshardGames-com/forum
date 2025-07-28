package com.iceshardgames.gamercommunity.Adapter;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.MediaController;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.exoplayer2.ExoPlayer;
import com.google.android.exoplayer2.MediaItem;
import com.google.android.exoplayer2.PlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.ui.PlayerView;
import com.iceshardgames.gamercommunity.Model.VideoModel;
import com.iceshardgames.gamercommunity.R;

import java.util.List;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder>{

private Context context;
private List<VideoModel> videoList;

public VideoAdapter(Context context, List<VideoModel> videoList) {
    this.context = context;
    this.videoList = videoList;
}

@NonNull
@Override
public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    View view = LayoutInflater.from(context).inflate(R.layout.video_grid_item, parent, false);
    return new VideoViewHolder(view);
}

@Override
public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
    VideoModel video = videoList.get(position);

    // Release any existing player before binding new data
    if (holder.exoPlayer != null) {
        holder.exoPlayer.release();
        holder.exoPlayer = null;
    }

    // Initialize ExoPlayer for this item
    holder.exoPlayer = new ExoPlayer.Builder(context).build();
    holder.videoPlayerView.setPlayer(holder.exoPlayer); // Attach player to PlayerView

    // Build the MediaItem from the video URL
    MediaItem mediaItem = MediaItem.fromUri(Uri.parse(video.getVideoUrl()));
    holder.exoPlayer.setMediaItem(mediaItem);

    // Set volume to 0 (mute)
    holder.exoPlayer.setVolume(0f);

    // Prepare the player
    holder.exoPlayer.prepare();

    // Set to loop the video
    holder.exoPlayer.setRepeatMode(Player.REPEAT_MODE_ONE);

    // Start playback
    holder.exoPlayer.play();

    // Add a listener for playback events (e.g., errors)
    holder.exoPlayer.addListener(new Player.Listener() {
        @Override
        public void onPlayerError(@NonNull PlaybackException error) {
//            String errorMessage = "ExoPlayer Error for " + video.getTitle() + ": " + error.getMessage();
//            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show();
//            Log.e("==videoadapter", errorMessage, error);
        }

        @Override
        public void onPlaybackStateChanged(int playbackState) {
            if (playbackState == Player.STATE_READY) {
                Log.d("==videoadapter", "ExoPlayer State Ready: " + video.getTitle());
                // Toast.makeText(context, "Video Prepared: " + video.getTitle(), Toast.LENGTH_SHORT).show(); // Optional: visual confirmation
            } else if (playbackState == Player.STATE_ENDED) {
                Log.d("==videoadapter", "ExoPlayer State Ended: " + video.getTitle());
                // Since repeat mode is ONE, it should loop automatically.
            }
        }
    });

    // Set a click listener for the entire item view
    holder.itemView.setOnClickListener(v -> {
        Toast.makeText(context, "Tapped on: " + video.getTitle(), Toast.LENGTH_SHORT).show();
        // You could toggle play/pause here or open a full-screen player
    });
}

    @Override
    public int getItemCount() {
        return videoList.size();
    }

    // This method is crucial for releasing players when views are recycled
    @Override
    public void onViewRecycled(@NonNull VideoViewHolder holder) {
        super.onViewRecycled(holder);
        if (holder.exoPlayer != null) {
            holder.exoPlayer.release();
            holder.exoPlayer = null;
            Log.d("==videoadapter", "ExoPlayer released for recycled view.");
        }
    }

    public static class VideoViewHolder extends RecyclerView.ViewHolder {
        PlayerView videoPlayerView; // Changed from VideoView to PlayerView
        public ExoPlayer exoPlayer; // Hold a reference to the ExoPlayer instance

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            videoPlayerView = itemView.findViewById(R.id.videoPlayer); // Still uses the same ID
        }
    }
}