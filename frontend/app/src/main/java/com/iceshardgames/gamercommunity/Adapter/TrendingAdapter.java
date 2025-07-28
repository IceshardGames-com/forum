package com.iceshardgames.gamercommunity.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Model.TrendingTopic;
import com.iceshardgames.gamercommunity.R;

import java.util.List;

public class TrendingAdapter extends RecyclerView.Adapter<TrendingAdapter.TrendingViewHolder> {

    private List<TrendingTopic> topicList;

    public TrendingAdapter(List<TrendingTopic> topicList) {
        this.topicList = topicList;
    }

    @NonNull
    @Override
    public TrendingViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.trending_item, parent, false);
        return new TrendingViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TrendingViewHolder holder, int position) {
        TrendingTopic topic = topicList.get(position);
        holder.title.setText(topic.getTitle());
        holder.searches.setText(topic.getSearches() + " searches");

        switch (topic.getTrendDirection()) {
            case "up":
                holder.icon.setImageResource(R.drawable.up_arrow);
                holder.icon.setColorFilter(holder.itemView.getContext().getResources().getColor(android.R.color.holo_green_light));
                break;
            case "down":
                holder.icon.setImageResource(R.drawable.down_arrow);
                holder.icon.setColorFilter(holder.itemView.getContext().getResources().getColor(android.R.color.holo_red_light));
                break;
            case "flat":
            default:
                holder.icon.setImageResource(R.drawable.minus);
                holder.icon.setColorFilter(holder.itemView.getContext().getResources().getColor(android.R.color.darker_gray));
                break;
        }
    }

    @Override
    public int getItemCount() {
        return topicList.size();
    }

    public static class TrendingViewHolder extends RecyclerView.ViewHolder {
        TextView title, searches;
        ImageView icon;

        public TrendingViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.topicTitle);
            searches = itemView.findViewById(R.id.topicSearches);
            icon = itemView.findViewById(R.id.trendIcon);
        }
    }
}