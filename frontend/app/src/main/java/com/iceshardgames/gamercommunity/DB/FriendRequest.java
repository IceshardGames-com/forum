// DB/FriendRequest.java
package com.iceshardgames.gamercommunity.DB;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "friend_requests")
public class FriendRequest {
    @PrimaryKey(autoGenerate = true)
    private int id;
    private String requestId; // From API
    private String userId;    // for block
    private String senderName;
    private String status; // pending / accepted / declined / blocked

    public FriendRequest(String requestId, String userId, String senderName, String status) {
        this.requestId = requestId;
        this.userId = userId;
        this.senderName = senderName;
        this.status = status;
    }
    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    // getters & setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }
    public String getSenderName() { return senderName; }
    public void setSenderName(String senderName) { this.senderName = senderName; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
