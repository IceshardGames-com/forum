// ConversationResponse.java
package com.iceshardgames.gamercommunity.Model.Response;

import com.google.gson.annotations.SerializedName;

import java.util.List;

public class ConversationResponse {
    private boolean success;
    private String message;
    private Data data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Data getData() { return data; }

    public static class Data {
        @SerializedName("conversationId")
        private String conversationId;

        private List<String> participants;
        private boolean isFriendBased;
        private String initiatedBy;   // can be null
        private String createdAt;

        public String getConversationId() { return conversationId; }
        public List<String> getParticipants() { return participants; }
        public boolean isFriendBased() { return isFriendBased; }
        public String getInitiatedBy() { return initiatedBy; }
        public String getCreatedAt() { return createdAt; }
    }
}
