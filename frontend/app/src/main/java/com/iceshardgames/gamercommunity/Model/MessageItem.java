package com.iceshardgames.gamercommunity.Model;

public class MessageItem {
    private String messageId;  // 🔥 NEW: ID from Room

    private String message;
    private boolean sentByUser;
    private String time;
    private String username;
    private String mediaUri; // <-- NEW FIELD
    private String fileType;

    // ✅ Constructor for media/text messages with ID
    public MessageItem(String messageId, String message, boolean sentByUser, String time, String username, String mediaUri, String fileType) {
        this.messageId = messageId;
        this.message = message;
        this.sentByUser = sentByUser;
        this.time = time;
        this.username = username;
        this.mediaUri = mediaUri;
        this.fileType = fileType;
    }

    // ✅ Constructor for text-only messages with ID
    public MessageItem(String messageId, String message, boolean sentByUser, String time, String username) {
        this(messageId, message, sentByUser, time, username, null, null);
    }


    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }


    public String getUsername() {
        return username;
    }

    public String getMessage() {
        return message;
    }

    public boolean isSentByUser() {
        return sentByUser;
    }

    public String getTime() {
        return time;
    }

    public String getMediaUri() {
        return mediaUri;
    }

    public void setMediaUri(String mediaUri) {
        this.mediaUri = mediaUri;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public void setSentByUser(boolean sentByUser) {
        this.sentByUser = sentByUser;
    }
    public String getFileType() { return fileType; }
    public void setFileType(String fileType) {
        this.fileType = fileType;
    }
}
