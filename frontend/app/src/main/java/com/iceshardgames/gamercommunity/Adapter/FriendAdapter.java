package com.iceshardgames.gamercommunity.Adapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Activity.ChatScreen.ChatDetailActivity1;
import com.iceshardgames.gamercommunity.Chat.ChatDetailActivity;
import com.iceshardgames.gamercommunity.Model.FriendListResponse;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;

import java.util.List;
public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private Context context;
    private List<FriendListResponse.Friend> friends;

    public FriendAdapter(Context context, List<FriendListResponse.Friend> friends) {
        this.context = context;
        this.friends = friends;
    }

    @NonNull
    @Override
    public FriendViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend, parent, false);
        return new FriendViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FriendViewHolder holder, int position) {
        FriendListResponse.Friend friend = friends.get(position);
        Log.d("==pass", "Binding: " + friend.getUsername());

        holder.username.setText(friend.getUsername());
        holder.role.setText(friend.getRole());
        holder.profilePic.setImageResource(R.drawable.profilepic);

        // ✅ handle click
        holder.itemView.setOnClickListener(v -> {
            // Open Chat Screen or Friend Profile
            // Example: open ChatDetailActivity
             Intent intent = new Intent(context, ChatDetailActivity.class);
             intent.putExtra("chat_id", friend.getId());
             intent.putExtra("chat_partner_name", friend.getUsername());
            intent.putExtra("other_user_id", friend.getId());  // REQUIRED

            context.startActivity(intent);

        });
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    public static class FriendViewHolder extends RecyclerView.ViewHolder {
        ImageView profilePic;
        TextView username, role;

        public FriendViewHolder(@NonNull View itemView) {
            super(itemView);
            profilePic = itemView.findViewById(R.id.friendProfilePic);
            username = itemView.findViewById(R.id.friendUsername);
            role = itemView.findViewById(R.id.friendRole);
        }
    }
}
