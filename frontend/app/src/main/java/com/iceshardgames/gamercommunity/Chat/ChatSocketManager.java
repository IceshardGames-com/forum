// ChatSocketManager.java
package com.iceshardgames.gamercommunity.Chat;

import android.util.Log;

import org.json.JSONObject;

import java.net.URLEncoder;

import io.socket.client.IO;
import io.socket.client.Socket;

public class ChatSocketManager {
    private Socket socket;
    private final String baseUrl;

    public interface Listener {
        void onNewMessage(String conversationId, String messageId, String senderId, String text, String createdAt);
        void onDelivered(String messageId);
        void onRead(String messageId);
        void onTyping(String conversationId, String userId, boolean isTyping);
    }

    public ChatSocketManager(String baseUrl){ this.baseUrl = baseUrl; }


    public void connect(String userId, String deviceId, String bearerToken, Listener listener){
        try {
            IO.Options opts = new IO.Options();
            opts.forceNew = true;
            opts.reconnection = true;
            opts.secure = true;
            opts.transports = new String[]{"websocket"}; // IMPORTANT on Render
            // If your server expects JWT in query (typical with socket.io v2):
            opts.query = "token=" + URLEncoder.encode(bearerToken.replace("Bearer ", ""), "UTF-8");

            socket = IO.socket(baseUrl, opts);

            socket.on(Socket.EVENT_CONNECT, args -> {
                // join both rooms
                socket.emit("join", userId);
                socket.emit("join", "device:" + deviceId);
                Log.d("SOCKET","connected");
            });

            // unified listener registrations
            socket.on("message:new", args -> {
                try {
                    JSONObject o = (JSONObject) args[0];

                    // server may send either {text} or {payload:{ciphertext}}
                    String text = o.optString("text", "");
                    if (text.isEmpty()) {
                        JSONObject p = o.optJSONObject("payload");
                        if (p != null) text = p.optString("ciphertext", "");
                    }

                    listener.onNewMessage(
                            o.getString("conversationId"),
                            o.optString("messageId",""),
                            o.optString("senderId",""),
                            text,
                            o.optString("createdAt","")
                    );
                } catch (Exception e) { Log.e("SOCKET","parse message", e); }
            });

            socket.on("message:delivered", a -> {
                listener.onDelivered(((JSONObject)a[0]).optString("messageId",""));
            });
            socket.on("message:read", a -> {
                listener.onRead(((JSONObject)a[0]).optString("messageId",""));
            });
            socket.on("typing", a -> {
                JSONObject o = (JSONObject) a[0];
                listener.onTyping(o.optString("conversationId",""), o.optString("userId",""), o.optBoolean("isTyping", false));
            });

            socket.connect();
        } catch (Exception e){ Log.e("SOCKET","connect", e); }
    }

    public void disconnect(){ if(socket!=null) socket.disconnect(); }

    public void listen(Listener listener){
        if(socket==null) return;

        socket.on("message:new", args -> {
            try {
                JSONObject o = (JSONObject) args[0];
                String convId = o.getString("conversationId");
                String messageId = o.getString("messageId");
                String sender = o.getString("senderId");
                // server sends payload per device; here we assume plain text in 'text' for now
                String text = o.optString("text", "");
                String createdAt = o.optString("createdAt", "");
                listener.onNewMessage(convId, messageId, sender, text, createdAt);
            } catch (Exception e){ Log.e("SOCKET","parse message", e); }
        });

        socket.on("message:delivered", a -> {
            String id = ((org.json.JSONObject)a[0]).optString("messageId","");
            listener.onDelivered(id);
        });
        socket.on("message:read", a -> {
            String id = ((org.json.JSONObject)a[0]).optString("messageId","");
            listener.onRead(id);
        });
        socket.on("typing", a -> {
            JSONObject o = (JSONObject) a[0];
            listener.onTyping(o.optString("conversationId",""), o.optString("userId",""), o.optBoolean("isTyping", false));
        });
    }
}
