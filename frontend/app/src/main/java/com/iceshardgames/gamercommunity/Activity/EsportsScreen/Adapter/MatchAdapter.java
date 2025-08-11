package com.iceshardgames.gamercommunity.Activity.EsportsScreen.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Activity.EsportsScreen.Model.EsportsMatch;
import com.iceshardgames.gamercommunity.R;

import java.util.List;

public class MatchAdapter extends RecyclerView.Adapter<MatchAdapter.MatchViewHolder> {

    private List<EsportsMatch> matches;
    private Context context;


    public MatchAdapter(Context context, List<EsportsMatch> matches) {
        this.context = context;
        this.matches = matches;
    }

    @NonNull
    @Override
    public MatchViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_match, parent, false);
        return new MatchViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MatchViewHolder holder, int position) {
        EsportsMatch match = matches.get(position);
        holder.title.setText(match.getTitle());
        holder.teams.setText(match.getTeamA() + "  vs  " + match.getTeamB());
        holder.score.setText(match.getScore());
        holder.prize.setText(match.getPrize());
        holder.liveNow.setText(match.getLiveNow());
        holder.viewCount.setText(formatViewers(match.getViewers()));
         holder.matchImage.setImageResource(R.drawable.img1);
    }

    @Override
    public int getItemCount() {
        return matches.size();
    }
    private String formatViewers(int viewers) {
        if (viewers >= 1000) return String.format("%.1fK", viewers / 1000.0);
        return String.valueOf(viewers);
    }
    static class MatchViewHolder extends RecyclerView.ViewHolder {
        TextView title, teams, score, prize, liveNow, viewCount;
        ImageView matchImage;
        TextView btnWatchNow;

        public MatchViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.matchTitle);
            teams = itemView.findViewById(R.id.teamsText);
            score = itemView.findViewById(R.id.matchScore);
            prize = itemView.findViewById(R.id.prizeBadge);
            liveNow = itemView.findViewById(R.id.matchTime);
            viewCount = itemView.findViewById(R.id.viewCount);
            btnWatchNow = itemView.findViewById(R.id.btnWatchNow);
            matchImage = itemView.findViewById(R.id.matchImage);
        }
    }
}

