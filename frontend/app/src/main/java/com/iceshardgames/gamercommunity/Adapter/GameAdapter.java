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

    public GameAdapter(Context context, List<GameModel> gameList) {
        this.context = context;
        this.gameList = gameList;
    }

    @Override
    public GameViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_trending_game, parent, false);
        return new GameViewHolder(view);
    }

    @Override
    public void onBindViewHolder(GameViewHolder holder, int position) {
        GameModel game = gameList.get(position);
        holder.title.setText(game.getTitle());
        holder.genre.setText(game.getGenre());
        holder.image.setImageResource(game.getImageResId());
        holder.rating.setText("" + game.getRating());
        holder.percetage.setText("+" + game.getGamePercetage()+"%");
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
    }
}
