package com.iceshardgames.gamercommunity.Adapter;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.Activity.ChatScreen.ChatDetailActivity;
import com.iceshardgames.gamercommunity.DB.AppDatabase;
import com.iceshardgames.gamercommunity.Model.ChatItem;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.SessionManager;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatAdapter extends RecyclerView.Adapter<ChatAdapter.ChatViewHolder> {
    private List<ChatItem> chatList;
    private Context context;


    public ChatAdapter(Context context, List<ChatItem> chatList) {
        this.context = context;
        this.chatList = chatList;
    }

    @NonNull
    @Override
    public ChatViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.chat_item, parent, false);
        return new ChatViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatViewHolder holder, int position) {
        ChatItem chat = chatList.get(position);
        holder.name.setText(chat.getName());
        holder.lastMessage.setText(chat.getLastMessage());
        holder.time.setText(chat.getLastMessageTime());
        holder.avatar.setImageResource(chat.getAvatarResId());
        if (chat.getUnreadCount() > 0) {
            holder.unreadBadge.setVisibility(View.VISIBLE);
            holder.unreadBadge.setText(String.valueOf(chat.getUnreadCount()));
        } else {
            holder.unreadBadge.setVisibility(View.GONE);
        }

        if (chat.isMuted()) {
            holder.muteIcon.setVisibility(View.VISIBLE);
        } else {
            holder.muteIcon.setVisibility(View.GONE);
        }

        if (chat.isPinned()) {
            holder.pinIcon.setVisibility(View.VISIBLE);
        } else {
            holder.pinIcon.setVisibility(View.GONE);
        }

        // 👇 Add this for chat click
        holder.itemView.setOnClickListener(v -> {
            Intent intent = new Intent(context, ChatDetailActivity.class);
            intent.putExtra("chat_id", chat.getChatId());
            intent.putExtra("chat_partner_name", chat.getName());
            // If your chatId is "dm_<friendId>", derive the other user id:
            String otherUserId = chat.getChatId() != null && chat.getChatId().startsWith("dm_")
                    ? chat.getChatId().substring(3)
                    : null;
            if (otherUserId != null) {
                intent.putExtra("other_user_id", otherUserId);
            }            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        });
        holder.itemView.setOnLongClickListener(v -> {
            showChatOptions(chat, position);
            return true;
        });

    }
    private void showChatOptions(ChatItem chat, int position) {
        String[] options = {"Pin", "Mute", "Delete"};
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Chat Options")
                .setItems(options, (dialog, which) -> {
                    switch (which) {
                        case 0: // Pin
                            chat.setPinned(!chat.isPinned());
                            Toast.makeText(context, chat.isPinned() ? "Chat pinned" : "Unpinned", Toast.LENGTH_SHORT).show();
                            notifyDataSetChanged();
                            break;
                        case 1: // Mute
                            chat.setMuted(!chat.isMuted());
                            Toast.makeText(context, chat.isMuted() ? "Chat muted" : "Unmuted", Toast.LENGTH_SHORT).show();
                            break;
                        case 2: // Delete
                            ExecutorService executor = Executors.newSingleThreadExecutor();
                            executor.execute(() -> {
                                // ✅ Get current logged-in userId
                                String currentUserId = SessionManager.getUserId(context);
                                if (currentUserId == null) currentUserId = "guest"; // fallback

                                // ✅ Use per-user database
                                AppDatabase db = AppDatabase.getInstance(context.getApplicationContext(), currentUserId);

                                db.chatUserDao().deleteById(chat.getChatId());
                                db.chatMessageDao().deleteMessagesByChatId(chat.getChatId());

                                // Update UI on main thread
                                new android.os.Handler(android.os.Looper.getMainLooper()).post(() -> {
                                    chatList.remove(position);
                                    notifyItemRemoved(position);
                                    Toast.makeText(context, "Chat deleted", Toast.LENGTH_SHORT).show();
                                });
                            });
                            break;
                    }
                }).show();
    }

    @Override
    public int getItemCount() {
        return chatList.size();
    }

    static class ChatViewHolder extends RecyclerView.ViewHolder {
        TextView name, lastMessage, time, unreadBadge ;
        ImageView avatar, muteIcon, pinIcon;

        public ChatViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.chatName);
            lastMessage = itemView.findViewById(R.id.chatMessage);
            time = itemView.findViewById(R.id.chatTime);
            unreadBadge = itemView.findViewById(R.id.unreadBadge);
            muteIcon = itemView.findViewById(R.id.muteIcon);
            pinIcon = itemView.findViewById(R.id.pinIcon);
            avatar = itemView.findViewById(R.id.chatAvatar);
        }
    }
}
