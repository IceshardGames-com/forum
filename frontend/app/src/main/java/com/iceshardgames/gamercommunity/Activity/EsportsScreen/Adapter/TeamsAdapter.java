package com.iceshardgames.gamercommunity.Activity.EsportsScreen.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Activity.EsportsScreen.Model.Team;
import com.iceshardgames.gamercommunity.R;

import java.util.List;

public class TeamsAdapter extends RecyclerView.Adapter<TeamsAdapter.TeamViewHolder> {

    private Context context;
    private List<Team> teamList;

    public TeamsAdapter(Context context, List<Team> teamList) {
        this.context = context;
        this.teamList = teamList;
    }

    @NonNull
    @Override
    public TeamViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_team, parent, false);
        return new TeamViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TeamViewHolder holder, int position) {
        Team team = teamList.get(position);

        holder.tvRank.setText(String.valueOf(team.getRank()));
        holder.ivLogo.setImageResource(team.getImageResId());
        holder.tvName.setText(team.getName());
        holder.tvPoints.setText("Points: " + team.getPoints());
        holder.tvWins.setText("W: " + team.getWins());
        holder.tvLosses.setText("L: " + team.getLosses());
    }

    @Override
    public int getItemCount() {
        return teamList.size();
    }

    public static class TeamViewHolder extends RecyclerView.ViewHolder {
        TextView tvRank, tvName, tvPoints, tvWins, tvLosses;
        ImageView ivLogo;

        public TeamViewHolder(@NonNull View itemView) {
            super(itemView);
            tvRank = itemView.findViewById(R.id.tvRank);
            ivLogo = itemView.findViewById(R.id.ivLogo);
            tvName = itemView.findViewById(R.id.tvName);
            tvPoints = itemView.findViewById(R.id.tvPoints);
            tvWins = itemView.findViewById(R.id.tvWins);
            tvLosses = itemView.findViewById(R.id.tvLosses);
        }
    }
}
