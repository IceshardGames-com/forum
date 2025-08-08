package com.iceshardgames.gamercommunity.Adapter;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Model.Game;
import com.iceshardgames.gamercommunity.R;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class SimilarGameAdapter extends RecyclerView.Adapter<SimilarGameAdapter.GameViewHolder> {

    public interface OnGameClickListener {
        void onGameClick(Game game);
    }

    private Context context;
    private List<Game> games;
    private OnGameClickListener listener;

    public SimilarGameAdapter(Context context, List<Game> games, OnGameClickListener listener) {
        this.context = context;
        this.games = games;
        this.listener = listener;
    }

    @NonNull
    @Override
    public GameViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_similar_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GameViewHolder holder, int position) {
        Game game = games.get(position);
        holder.title.setText(game.getTitle());
        holder.rating.setText(String.valueOf(game.getRating()));
        // You can also set icon/image here if needed

        holder.itemView.setOnClickListener(v -> listener.onGameClick(game));
    }

    @Override
    public int getItemCount() {
        return games.size();
    }

    static class GameViewHolder extends RecyclerView.ViewHolder {
        TextView title, rating;
        ImageView gameIcon;

        public GameViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.textGameTitle);
            rating = itemView.findViewById(R.id.textGameRating);
            gameIcon = itemView.findViewById(R.id.imageGameIcon);
        }
    }
}
