package com.iceshardgames.gamercommunity.Adapter;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Fragment.ForumDetailFragment;
import com.iceshardgames.gamercommunity.Fragment.PostDetailFragment;
import com.iceshardgames.gamercommunity.Model.PostModel;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;

import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {

    List<PostModel> posts;
    Activity activity;

    public PostAdapter(FragmentActivity activity, List<PostModel> posts) {
        this.activity = activity;
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
        holder.title.setText(post.getTitle());     // Post Title
        holder.meta.setText("by " + post.getName());  // ✅ show name from inputName

        holder.replies.setText(post.getReplies() + " replies");
        holder.likes.setText(post.getLikes() + " likes");
        // ✅ set post time
        holder.postTime.setText(Utills.getTimeAgo(post.getCreatedAt()));

        holder.itemView.setOnClickListener(v -> {
            PostDetailFragment fragment = PostDetailFragment.newInstance(
                    post.getPostId(),
                    post.getTitle(),
                    post.getAuthor(),
                    post.getDisLikes(),
                    post.getReplies(),
                    post.getCreatedAt() ,
                    post.getLikes(),// <-- pass millis
                    post.getAuthorId()// <-- pass millis,
            );

            ((AppCompatActivity) activity).getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.fragment_container, fragment) // use your container id
                    .addToBackStack(null)
                    .commit();
        });

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
        TextView title, meta, replies, likes,postTime;
        ImageView pinIcon;

        public PostViewHolder(@NonNull View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.postTitle);
            meta = itemView.findViewById(R.id.postMeta);
            replies = itemView.findViewById(R.id.postReplies);
            likes = itemView.findViewById(R.id.postLikes);
            pinIcon = itemView.findViewById(R.id.pinIcon);
            postTime = itemView.findViewById(R.id.postTime);
        }
    }
}
