// SendMessageRequest.java
package com.iceshardgames.gamercommunity.Model.Request;

import com.iceshardgames.gamercommunity.Model.EncryptedPayload;

import java.util.List;

public class SendMessageRequest {
    private java.util.List<EncryptedPayload> payloads;
    private String messageType; // "text"
    private Object meta;        // optional
    public SendMessageRequest(List<EncryptedPayload> payloads, String messageType, Object meta){
        this.payloads = payloads; this.messageType = messageType; this.meta = meta;
    }
}
