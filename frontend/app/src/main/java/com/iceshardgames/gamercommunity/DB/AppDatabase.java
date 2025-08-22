package com.iceshardgames.gamercommunity.DB;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

import java.util.HashMap;
import java.util.Map;

@Database(entities = {
        ChatUser.class,
        ChatMessage.class,
        Channel.class,
        FriendRequest.class   // ✅ IMPORTANT: Add this line
}, version = 7, exportSchema = false)

public abstract class AppDatabase extends RoomDatabase {

    private static final Map<String, AppDatabase> INSTANCES = new HashMap<>();

    public abstract ChatUserDao chatUserDao();
    public abstract ChatMessageDao chatMessageDao();
    public abstract ChannelDao channelDao();
    public abstract FriendRequestDao friendRequestDao();

    // ✅ Make DB unique per logged-in user
    public static synchronized AppDatabase getInstance(Context context, String userId) {
        if (userId == null || userId.trim().isEmpty()) {
            userId = "guest"; // fallback for safety
        }

        if (!INSTANCES.containsKey(userId)) {
            AppDatabase instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "chat_app_database_" + userId // unique DB per user
                    )
                    .fallbackToDestructiveMigration()
                    .build();
            INSTANCES.put(userId, instance);
        }

        return INSTANCES.get(userId);
    }
}
