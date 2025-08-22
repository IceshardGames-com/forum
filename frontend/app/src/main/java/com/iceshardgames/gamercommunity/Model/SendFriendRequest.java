package com.iceshardgames.gamercommunity.Model;

public class SendFriendRequest {
    private String recipientId;

    public SendFriendRequest(String recipientId) {
        this.recipientId = recipientId;
    }

    public String getRecipientId() {
        return recipientId;
    }

    public void setRecipientId(String recipientId) {
        this.recipientId = recipientId;
    }
}

