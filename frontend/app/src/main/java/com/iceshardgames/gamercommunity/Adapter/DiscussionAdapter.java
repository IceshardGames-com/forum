package com.iceshardgames.gamercommunity.Adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Model.DiscussionModel;
import com.iceshardgames.gamercommunity.R;

import java.util.List;

public class DiscussionAdapter extends RecyclerView.Adapter<DiscussionAdapter.DiscussionViewHolder> {

    private List<DiscussionModel> discussionList;

    public DiscussionAdapter(List<DiscussionModel> discussionList) {
        this.discussionList = discussionList;
    }

    @NonNull
    @Override
    public DiscussionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_discussion, parent, false);
        return new DiscussionViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DiscussionViewHolder holder, int position) {
        DiscussionModel model = discussionList.get(position);

        holder.tag.setText(model.getTag());
        holder.title.setText(model.getTitle());
        holder.author.setText("by " + model.getAuthor());
        holder.replies.setText(model.getReplies() + " replies");
        holder.timeAgo.setText(model.getTimeAgo());
        holder.tvLikes.setText(model.getTvLikes());
    }

    @Override
    public int getItemCount() {
        return discussionList.size();
    }

    static class DiscussionViewHolder extends RecyclerView.ViewHolder {
        TextView tag, title, author, replies, timeAgo, tvLikes;

        DiscussionViewHolder(@NonNull View itemView) {
            super(itemView);
            tag = itemView.findViewById(R.id.topicTag);
            title = itemView.findViewById(R.id.title);
            author = itemView.findViewById(R.id.authorInfo);
            replies = itemView.findViewById(R.id.tvReplies);
            timeAgo = itemView.findViewById(R.id.tvTimeAgo);
            tvLikes = itemView.findViewById(R.id.tvLikes);
        }
    }
}