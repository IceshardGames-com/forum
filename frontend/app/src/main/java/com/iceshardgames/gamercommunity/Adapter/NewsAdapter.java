package com.iceshardgames.gamercommunity.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Model.NewsModel;
import com.iceshardgames.gamercommunity.R;

import java.util.List;

public class NewsAdapter extends RecyclerView.Adapter<NewsAdapter.NewsViewHolder> {

    private Context context;
    private List<NewsModel> newsList;

    public NewsAdapter(Context context, List<NewsModel> newsList) {
        this.context = context;
        this.newsList = newsList;
    }

    @Override
    public NewsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_news, parent, false);
        return new NewsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NewsViewHolder holder, int position) {
        NewsModel news = newsList.get(position);
        holder.source.setText(news.getSource());
        holder.label.setText(news.getLabel());
        holder.title.setText(news.getTitle());
        holder.time.setText(news.getTime());
    }

    @Override
    public int getItemCount() {
        return newsList.size();
    }

    public static class NewsViewHolder extends RecyclerView.ViewHolder {
        TextView source, label, title, time;

        public NewsViewHolder(View itemView) {
            super(itemView);
            source = itemView.findViewById(R.id.newsSource);
            label = itemView.findViewById(R.id.newsLabel);
            title = itemView.findViewById(R.id.newsTitle);
            time = itemView.findViewById(R.id.newsTime);
        }
    }
}
