package com.iceshardgames.gamercommunity.Fragment;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.R;

import java.util.List;

public class ForumAdapter extends RecyclerView.Adapter<ForumAdapter.ForumViewHolder> {
    private Context context;

    List<ForumModel> forumList;

    public ForumAdapter(Context context, List<ForumModel> forumList) {
        this.context = context;
        this.forumList = forumList;
    }

    @NonNull
    @Override
    public ForumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.forum_item, parent, false);
        return new ForumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ForumViewHolder holder, int position) {
        ForumModel forum = forumList.get(position);
        holder.title.setText(forum.getTitle());
        holder.stats.setText(forum.getStats());
        holder.lastActive.setText(forum.getLastActive());
        holder.status.setText(forum.getStatus());
        holder.image.setImageResource(forum.getImageResId());

        holder.itemView.setOnClickListener(v -> {
            FragmentManager fragmentManager = ((AppCompatActivity) context).getSupportFragmentManager();
            ForumDetailFragment detailFragment = new ForumDetailFragment();

            Bundle bundle = new Bundle();
            bundle.putString("forum_title", forumList.get(position).getTitle());
            detailFragment.setArguments(bundle);

            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }

    @Override
    public int getItemCount() {
        return forumList.size();
    }

    static class ForumViewHolder extends RecyclerView.ViewHolder {
        TextView title, stats, lastActive, status;
        ImageView image;

        public ForumViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.forumTitle);
            stats = itemView.findViewById(R.id.forumStats);
            lastActive = itemView.findViewById(R.id.forumLastActive);
            status = itemView.findViewById(R.id.forumStatus);
            image = itemView.findViewById(R.id.forumImage);
        }
    }
}
