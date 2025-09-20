package com.iceshardgames.gamercommunity.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

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
    // new fields for validation
    private String myUserId;
    private String forumOwner;
    private String postPermission;
    private boolean isFollowing;
    private boolean isJoined;
    private String forumId;
    public PostAdapter(FragmentActivity activity, List<PostModel> posts,
                       String myUserId,
                       String forumOwner,
                       String postPermission,
                       boolean isFollowing,
                       boolean isJoined,
                       String forumId)
    {
        this.activity = activity;
        this.posts = posts;
        this.myUserId = myUserId;
        this.forumOwner = forumOwner;
        this.postPermission = postPermission;
        this.isFollowing = isFollowing;
        this.isJoined = isJoined;
        this.forumId = forumId;
    }

    public void updateFollowJoinState(boolean isFollowing, boolean isJoined) {
        this.isFollowing = isFollowing;
        this.isJoined = isJoined;
        notifyDataSetChanged(); // optional, but safe if UI depends on it
    }

    // helper to update permission or owner if needed
    public void updateForumMeta(String myUserId, String forumOwner, String postPermission, String forumId) {
        this.myUserId = myUserId;
        this.forumOwner = forumOwner;
        this.postPermission = postPermission;
        this.forumId = forumId;
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
            Log.e("==postPermission", "post: "+postPermission + " - " + isFollowing );
            // replicate validation logic from fragment before navigation
            if (!myUserId.equals(forumOwner)) {
                if(postPermission.equals("admin_only"))
                {
                    Toast.makeText(activity, "Only admin can see the post", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (postPermission.equals("followers") && !isFollowing) {
                    Toast.makeText(activity, "You must follow this forum to see the post", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (postPermission.equals("members") && !isJoined) {
                    Toast.makeText(activity, "You must join this forum to see the post", Toast.LENGTH_SHORT).show();
                    return;
                }

                if (postPermission.equals("members")) {
                    // Check paid membership
                    SharedPreferences prefs = activity.getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);

                    boolean isPaid = prefs.getBoolean("isPaidMember_" + forumId, false); // you need to set this when verifying payment
                    if (!isPaid) {
                        Toast.makeText(activity, "Only paid members can see the post", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }
            }
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
