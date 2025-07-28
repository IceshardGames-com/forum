package com.iceshardgames.gamercommunity.Activity.MainScreen;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.R;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.ViewHolder> {

    String[] dummyData = {
            "r/Needafriend\n17f need friends. • 1h ago",
            "r/VRGames\nCheck out our new update! • 2h ago"
    };

    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position, String data);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_notification, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        holder.textView.setText(dummyData[position]);

        if (position == 0) {
            holder.itemView.setPadding(0, 0, 0, 0);
        } else {
            ViewGroup.MarginLayoutParams params = (ViewGroup.MarginLayoutParams) holder.itemView.getLayoutParams();
            params.topMargin = 20;
            holder.itemView.setLayoutParams(params);
        }

        holder.itemView.setOnClickListener(v -> {
            if (listener != null) {
                listener.onItemClick(position, dummyData[position]);
            }
        });

    }

    @Override
    public int getItemCount() {
        return dummyData.length;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textView;
        public ViewHolder(View v) {
            super(v);
            textView = v.findViewById(R.id.notificationText);
        }
    }
}