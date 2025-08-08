package com.iceshardgames.gamercommunity.DB;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;

@Database(entities = {
        ChatUser.class,
        ChatMessage.class,
        Channel.class   // ✅ IMPORTANT: Add this line
}, version = 4, exportSchema = false)

public abstract class AppDatabase extends RoomDatabase {
    private static AppDatabase instance;

    public abstract ChatUserDao chatUserDao();

    public abstract ChatMessageDao chatMessageDao();

    public abstract ChannelDao channelDao();

    public static synchronized AppDatabase getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(
                            context.getApplicationContext(),
                            AppDatabase.class,
                            "chat_app_database"
                    )
                    // WARNING: this will delete data when version changes.
                    .fallbackToDestructiveMigration()
                    .build();
        }
        return instance;
    }
}
