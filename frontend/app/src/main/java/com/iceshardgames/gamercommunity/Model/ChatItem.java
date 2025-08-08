package com.iceshardgames.gamercommunity.Model;

public class ChatItem {
    private String chatId;
    private String name;
    private String lastMessage;
    private String lastMessageTime;
    private int avatarResId;
    private boolean isPinned;
    private boolean isMuted;
    private int unreadCount; // 👈 NEW FIELD

    public ChatItem(String chatId, String name, String lastMessage, String lastMessageTime,
                    int avatarResId, boolean isPinned, boolean isMuted) {
        this.chatId = chatId;
        this.name = name;
        this.lastMessage = lastMessage;
        this.lastMessageTime = lastMessageTime;
        this.avatarResId = avatarResId;
        this.isPinned = isPinned;
        this.isMuted = isMuted;
        this.unreadCount = 0; // 👈 default

    }
    // ✅ Getter & Setter for unreadCount
    public int getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(int unreadCount) {
        this.unreadCount = unreadCount;
    }
    // Getters
    public String getChatId() {
        return chatId;
    }

    public String getName() {
        return name;
    }

    public String getLastMessage() {
        return lastMessage;
    }

    public String getLastMessageTime() {
        return lastMessageTime;
    }

    public int getAvatarResId() {
        return avatarResId;
    }

    public boolean isPinned() {
        return isPinned;
    }

    public boolean isMuted() {
        return isMuted;
    }

    // Setters
    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setLastMessage(String lastMessage) {
        this.lastMessage = lastMessage;
    }

    public void setLastMessageTime(String lastMessageTime) {
        this.lastMessageTime = lastMessageTime;
    }

    public void setAvatarResId(int avatarResId) {
        this.avatarResId = avatarResId;
    }

    public void setPinned(boolean pinned) {
        isPinned = pinned;
    }

    public void setMuted(boolean muted) {
        isMuted = muted;
    }
}

