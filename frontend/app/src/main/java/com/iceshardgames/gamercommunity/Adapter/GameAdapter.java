package com.iceshardgames.gamercommunity.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Model.GameModel;
import com.iceshardgames.gamercommunity.R;

import java.util.List;

public class GameAdapter extends RecyclerView.Adapter<GameAdapter.GameViewHolder> {

    private Context context;
    private List<GameModel> gameList;
    private static OnGameClickListener listener;

    public GameAdapter(Context context, List<GameModel> gameList, OnGameClickListener listener) {
        this.context = context;
        this.gameList = gameList;
        this.listener = listener;
    }

    public interface OnGameClickListener {
        void onGameClick(GameModel game);
    }
    @Override
    public GameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trending_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GameViewHolder holder, int position) {
        GameModel game = gameList.get(position);
        holder.bind(game);
    }

    @Override
    public int getItemCount() {
        return gameList.size();
    }

    public static class GameViewHolder extends RecyclerView.ViewHolder {
        ImageView image;
        TextView title, genre, rating, percetage;

        public GameViewHolder(View itemView) {
            super(itemView);
            image = itemView.findViewById(R.id.gameImage);
            title = itemView.findViewById(R.id.gameTitle);
            genre = itemView.findViewById(R.id.gameGenre);
            rating = itemView.findViewById(R.id.gameRating);
            percetage = itemView.findViewById(R.id.gamePercetage);
        }

        public void bind(GameModel game) {
            title.setText(game.getTitle());
            genre.setText(game.getGenre());
            image.setImageResource(game.getImageResId());
            rating.setText("" + game.getRating());
            percetage.setText("+" + game.getGamePercetage()+"%");

            itemView.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onGameClick(game);
                }
            });
        }
    }
}
