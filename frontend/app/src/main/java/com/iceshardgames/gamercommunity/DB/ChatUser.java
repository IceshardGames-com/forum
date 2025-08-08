package com.iceshardgames.gamercommunity.DB;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_users")
public class ChatUser {
    @PrimaryKey
    @NonNull
    public String chatId;  // Unique chat identifier

    public String userName;
    public String lastMessage;
    private String lastMessageTime;

    public long timestamp;
    public boolean isPinned;
    public boolean isMuted;
    private int avatarResId; // Use drawable resource ID or change to URL if using Glide

    public ChatUser(@NonNull String chatId, String userName, String lastMessage, String lastMessageTime, long timestamp, boolean isPinned, boolean isMuted,int avatarResId) {
        this.chatId = chatId;
        this.userName = userName;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.timestamp = timestamp;
        this.isPinned = isPinned;
        this.isMuted = isMuted;
        this.avatarResId = avatarResId;
    }

    // Getters and setters for the fields
    // ...


    // Getters
    @NonNull
    public String getChatId() {
        return chatId;
    }

    public String getUserName() {
        return userName;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public boolean isMuted() {
        return isMuted;
    }

    public int getAvatarResId() {
        return avatarResId;
    }

    // Setters
    public void setChatId(@NonNull String chatId) {
        this.chatId = chatId;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }

    public void setAvatarResId(int avatarResId) {
        this.avatarResId = avatarResId;
    }

}

