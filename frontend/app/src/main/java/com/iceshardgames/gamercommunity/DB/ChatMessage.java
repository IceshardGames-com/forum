package com.iceshardgames.gamercommunity.DB;

import androidx.annotation.NonNull;
import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessage {
    @PrimaryKey
    @NonNull
    @ColumnInfo(name = "id")
    private String id;                  // unique per message (UUID or server ID)

    @ColumnInfo(name = "chat_id")
    public String chatId;

    @ColumnInfo(name = "server_message_id")
    private String serverMessageId;     // real id from API (nullable until ack)

    @ColumnInfo(name = "message")
    public String message;

    @ColumnInfo(name = "is_sent_by_user")
    public boolean isSentByUser;

    @ColumnInfo(name = "timestamp")
    public long timestamp;

    @ColumnInfo(name = "file_uri")
    public String fileUri;

    @ColumnInfo(name = "file_type")
    public String fileType;
    private boolean sentByMe; // true if current user sent this

    public ChatMessage() {
    }

    public ChatMessage(String chatId, String message, boolean isSentByUser, long timestamp) {
        this.id = java.util.UUID.randomUUID().toString(); // ✅ ensure non-null primary key
        this.chatId = chatId;
        this.message = message;
        this.isSentByUser = isSentByUser;
        this.timestamp = timestamp;
    }
    public boolean isSentByMe() { return sentByMe; }
    public void setSentByMe(boolean sentByMe) { this.sentByMe = sentByMe; }
    public String getChatId() {
        return chatId;
    }

    public void setChatId(String chatId) {
        this.chatId = chatId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSentByUser() {
        return isSentByUser;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }

    public String getFileUri() {
        return fileUri;
    }

    public void setFileUri(String fileUri) {
        this.fileUri = fileUri;
    }

    public String getFileType() {
        return fileType;
    }

    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
    // ---- Getters / Setters ----
    @NonNull
    public String getId() { return id; }
    public void setId(@NonNull String id) { this.id = id; }
    public String getServerMessageId() { return serverMessageId; }
    public void setServerMessageId(String serverMessageId) { this.serverMessageId = serverMessageId; }

}
