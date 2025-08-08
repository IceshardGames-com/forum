package com.iceshardgames.gamercommunity.Adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.DB.AppDatabase;
import com.iceshardgames.gamercommunity.DB.Channel;
import com.iceshardgames.gamercommunity.R;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChannelAdapter extends RecyclerView.Adapter<ChannelAdapter.ChannelViewHolder> {

    private final Context context;
    private final List<Channel> channels;

    public ChannelAdapter(Context context, List<Channel> channels) {
        this.context = context;
        this.channels = channels;
    }

    @NonNull
    @Override
    public ChannelViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_channel, parent, false);
        return new ChannelViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChannelViewHolder holder, int position) {
        Channel channel = channels.get(position);
        holder.name.setText(channel.name);
        holder.userCount.setText(channel.userCount + " Users");
        holder.icon.setImageResource(channel.iconResId);
        holder.joinButton.setText(channel.isJoined ? "Joined" : "Join");

        holder.joinButton.setOnClickListener(v -> {
            channel.isJoined = !channel.isJoined;
            holder.joinButton.setText(channel.isJoined ? "Joined" : "Join");

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                AppDatabase db = AppDatabase.getInstance(context);
                db.channelDao().update(channel);
            });
        });
    }

    @Override
    public int getItemCount() {
        return channels.size();
    }

    static class ChannelViewHolder extends RecyclerView.ViewHolder {
        ImageView icon;
        TextView name, userCount;
        TextView joinButton;

        public ChannelViewHolder(@NonNull View itemView) {
            super(itemView);
            icon = itemView.findViewById(R.id.channel_icon);
            name = itemView.findViewById(R.id.channel_name);
            userCount = itemView.findViewById(R.id.channel_user_count);
            joinButton = itemView.findViewById(R.id.join_button);
        }
    }
}
