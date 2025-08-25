// MessagesResponse.java
package com.iceshardgames.gamercommunity.Model;

import java.util.List;
// MessagesResponse.java

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MessagesResponse {
    private boolean success;
    private String message;
    private Data data;

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public Data getData() { return data; }

    public static class Data {
        private List<MessageNet> messages;
        private Pagination pagination;

        public List<MessageNet> getMessages() { return messages; }
        public Pagination getPagination() { return pagination; }
    }

    public static class Pagination {
        private int page;
        private int limit;
        private int total;
        private int pages;
        // getters...
        public int getPage(){return page;}
        public int getLimit(){return limit;}
        public int getTotal(){return total;}
        public int getPages(){return pages;}
    }

    public static class MessageNet {
        @SerializedName("messageId") private String messageId;
        @SerializedName("conversationId") private String conversationId;
        private Sender sender;
        private String deliveredAt; // add
        private String readAt;      // add
        private List<Payload> payloads;
        private String messageType;
        private boolean isEdited;
        private List<String> deliveredTo;
        private List<String> readBy;
        private String createdAt;

        public String getMessageId(){ return messageId; }
        public String getConversationId(){ return conversationId; }
        public Sender getSender(){ return sender; }
        public List<Payload> getPayloads(){ return payloads; }
        public String getMessageType(){ return messageType; }
        public String getCreatedAt(){ return createdAt; }
        public String getDeliveredAt() { return deliveredAt; } // add getter
        public String getReadAt() { return readAt; }           // add getter
        public static class Sender {
            @SerializedName("_id") private String id;
            private String username;
            private String email;
            public String getId(){ return id; }
            public String getUsername(){ return username; }
            public String getEmail(){ return email; }
        }

        public static class Payload {
            private String deviceId;
            private String ciphertext;
            public String getDeviceId(){ return deviceId; }
            public String getCiphertext(){ return ciphertext; }
        }
    }
}
