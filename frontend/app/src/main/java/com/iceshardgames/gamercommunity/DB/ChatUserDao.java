package com.iceshardgames.gamercommunity.DB;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatUserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insert(ChatUser user);

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertOrUpdate(ChatUser user);
    @Query("SELECT * FROM chat_users ORDER BY lastMessageTime DESC")
    List<ChatUser> getAllUsers();

    @Query("SELECT * FROM chat_users ORDER BY isPinned DESC, timestamp DESC")
    List<ChatUser> getAllChats();

    @Query("DELETE FROM chat_users WHERE chatId = :chatId")
    void deleteById(String chatId);

    @Query("UPDATE chat_users SET isPinned = :pinned WHERE chatId = :chatId")
    void setPinned(String chatId, boolean pinned);

    @Query("UPDATE chat_users SET isMuted = :muted WHERE chatId = :chatId")
    void setMuted(String chatId, boolean muted);
    @Query("SELECT * FROM chat_users WHERE chatId = :chatId LIMIT 1")
    ChatUser getUserByChatId(String chatId);

}

