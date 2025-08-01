package com.iceshardgames.gamercommunity.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Model.PostModel;
import com.iceshardgames.gamercommunity.R;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    List<PostModel> posts;

    public PostAdapter(List<PostModel> posts) {
        this.posts = posts;
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_post, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        PostModel post = posts.get(position);
        holder.title.setText(post.getTitle());
        holder.meta.setText("by " + post.getAuthor());
        holder.replies.setText(post.getReplies() + " replies");
        holder.likes.setText(post.getLikes() + " likes");

        if (post.isPinned()) {
            holder.pinIcon.setVisibility(View.VISIBLE);
        } else {
            holder.pinIcon.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return posts.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView title, meta, replies, likes;
        ImageView pinIcon;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.postTitle);
            meta = itemView.findViewById(R.id.postMeta);
            replies = itemView.findViewById(R.id.postReplies);
            likes = itemView.findViewById(R.id.postLikes);
            pinIcon = itemView.findViewById(R.id.pinIcon);
        }
    }
}
