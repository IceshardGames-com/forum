// MessageDto.java
package com.iceshardgames.gamercommunity.Model;

import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

public class MessageDto {
    private String id;
    private String sender;
    private String messageType;
    private String text;
    private String status;
    private String mediaUri;
    private String fileType;

    // Single source of truth for time used by UI
    private long sentAt; // epoch millis (0 = unknown)

    public MessageDto() {
        this.id = UUID.randomUUID().toString();
    }

    public MessageDto(String id, String sender, String text, long sentAt) {
        this.id = (id == null || id.isEmpty()) ? UUID.randomUUID().toString() : id;
        this.sender = sender;
        this.text = text;
        this.sentAt = sentAt;
    }

    public String getId() { return id; }
    public void setId(String id) {
        this.id = (id == null || id.isEmpty()) ? UUID.randomUUID().toString() : id;
    }

    public String getSender() { return sender; }
    public void setSender(String sender) { this.sender = sender; }

    public String getMessageType() { return messageType; }
    public void setMessageType(String messageType) { this.messageType = messageType; }

    public String getText() { return text; }
    public void setText(String text) { this.text = text; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public String getMediaUri() { return mediaUri; }
    public void setMediaUri(String mediaUri) { this.mediaUri = mediaUri; }

    public String getFileType() { return fileType; }
    public void setFileType(String fileType) { this.fileType = fileType; }

    public long getSentAt() { return sentAt; }
    public void setSentAt(long sentAt) { this.sentAt = sentAt; }

    /** If backend returns epoch seconds, convert to millis. */
    public void setSentAtSeconds(long epochSeconds) { this.sentAt = epochSeconds * 1000L; }

    /** If backend returns ISO-8601 string (e.g., 2025-08-25T07:41:22.531Z). */
    public void setSentAtFromIso(String isoUtc) {
        if (isoUtc == null || isoUtc.isEmpty()) { this.sentAt = 0L; return; }
        try {
            if (Build.VERSION.SDK_INT >= 26) {
                this.sentAt = java.time.Instant.parse(isoUtc).toEpochMilli();
                return;
            }
        } catch (Exception ignored) { /* fall through to fallback */ }

        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            this.sentAt = sdf.parse(isoUtc).getTime();
        } catch (Exception e) {
            this.sentAt = 0L;
        }
    }
}
