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

import com.iceshardgames.gamercommunity.Activity.ChatScreen.ChatDetailActivity;
import com.iceshardgames.gamercommunity.DB.AppDatabase;
import com.iceshardgames.gamercommunity.DB.ChatUser;
import com.iceshardgames.gamercommunity.Model.Response.FriendListResponse;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class FriendAdapter extends RecyclerView.Adapter<FriendAdapter.FriendViewHolder> {

    private Context context;
    private List<FriendListResponse.Friend> friends;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();

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
            String friendId = friend.getId();
            String friendName = friend.getUsername();
            String chatId = "dm_" + friendId; // deterministic per friend to avoid duplicates

            executor.execute(() -> {
                // per-user DB (same as elsewhere)
                String currentUserId = SessionManager.getUserId(context);
                if (currentUserId == null) currentUserId = "guest";

                AppDatabase db = AppDatabase.getInstance(context.getApplicationContext(), currentUserId);

                // Insert-or-update ChatUser (primary key = chatId)
                ChatUser chatUser = new ChatUser(
                        chatId,
                        friendName,
                        "",          // lastMessage
                        "",          // avatarUrl (set if you have one)
                        System.currentTimeMillis(),
                        false,       // pinned
                        false,       // muted
                        R.drawable.profilepic
                );
                db.chatUserDao().insertOrUpdate(chatUser);

                // Jump to the chat screen
                ((Activity) context).runOnUiThread(() -> {
                    Intent intent = new Intent(context, ChatDetailActivity.class);
                    intent.putExtra("chat_id", chatId);
                    intent.putExtra("chat_partner_name", friendName);
                    intent.putExtra("other_user_id", friendId); // keep if your chat screen needs it
                    context.startActivity(intent);
                });
            });
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
