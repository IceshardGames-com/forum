package com.iceshardgames.gamercommunity.Activity.EsportsScreen.Adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Activity.EsportsScreen.Model.CompletedMatch;
import com.iceshardgames.gamercommunity.R;

import java.util.List;

public class CompletedMatchesAdapter extends RecyclerView.Adapter<CompletedMatchesAdapter.ViewHolder> {

    private Context context;
    private List<CompletedMatch> completedMatches;

    public CompletedMatchesAdapter(Context context, List<CompletedMatch> completedMatches) {
        this.context = context;
        this.completedMatches = completedMatches;
    }

    @NonNull
    @Override
    public CompletedMatchesAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_completed_match, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull CompletedMatchesAdapter.ViewHolder holder, int position) {
        CompletedMatch match = completedMatches.get(position);
        holder.team1Name.setText(match.getTeam1Name());
        holder.team2Name.setText(match.getTeam2Name());
        holder.score.setText(match.getScore());
        holder.matchDate.setText(match.getMatchDate());
        holder.team1Logo.setImageResource(match.getTeam1LogoResId());
        holder.team2Logo.setImageResource(match.getTeam2LogoResId());
    }

    @Override
    public int getItemCount() {
        return completedMatches.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView team1Name, team2Name, score, matchDate;
        ImageView team1Logo, team2Logo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            team1Name = itemView.findViewById(R.id.tvTeamAName);
            team2Name = itemView.findViewById(R.id.tvTeamBName);
            score = itemView.findViewById(R.id.tvMatchScore);
            matchDate = itemView.findViewById(R.id.tvMatchDate);
            team1Logo = itemView.findViewById(R.id.ivTeamALogo);
            team2Logo = itemView.findViewById(R.id.ivTeamBLogo);
        }
    }

    public void updateData(List<CompletedMatch> newList) {
        this.completedMatches.clear();
        this.completedMatches.addAll(newList);
        notifyDataSetChanged();
    }
}