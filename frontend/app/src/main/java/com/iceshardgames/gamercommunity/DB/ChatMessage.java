package com.iceshardgames.gamercommunity.DB;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "chat_messages")
public class ChatMessage {

    @PrimaryKey(autoGenerate = true)
    public int id;

    @ColumnInfo(name = "chat_id")
    public String chatId;

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

    public ChatMessage(String chatId, String message, boolean isSentByUser, long timestamp) {
        this.chatId = chatId;
        this.message = message;
        this.isSentByUser = isSentByUser;
        this.timestamp = timestamp;
    }

    public String getChatId() {
        return chatId;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSentByUser() {
        return isSentByUser;
    }

    public long getTimestamp() {
        return timestamp;
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
}
