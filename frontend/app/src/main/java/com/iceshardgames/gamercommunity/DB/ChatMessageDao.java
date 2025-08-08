package com.iceshardgames.gamercommunity.DB;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface ChatMessageDao {

    @Insert
    void insert(ChatMessage message);

    // Get all messages for a specific chat (already exists)
    @Query("SELECT * FROM chat_messages WHERE chat_id = :chatId ORDER BY timestamp ASC")
    List<ChatMessage> getMessagesForChat(String chatId);

    // ✅ NEW: Get last message for a specific chat
    @Query("SELECT * FROM chat_messages WHERE chat_id = :chatId ORDER BY timestamp DESC LIMIT 1")
    ChatMessage getLastMessageForChat(String chatId);

    // ✅ NEW: Delete message by ID
    @Query("SELECT * FROM chat_messages WHERE chat_id = :chatId")
    ChatMessage getMessageById(String chatId);

    @Query("DELETE FROM chat_messages WHERE chat_id = :chatId")
    void deleteMessagesByChatId(String chatId);

}

