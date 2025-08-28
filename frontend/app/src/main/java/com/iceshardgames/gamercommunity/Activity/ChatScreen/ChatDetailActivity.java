package com.iceshardgames.gamercommunity.Activity.ChatScreen;

import android.os.Build;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.APIintegration.ApiClient;
import com.iceshardgames.gamercommunity.APIintegration.ApiService;
import com.iceshardgames.gamercommunity.DB.AppDatabase;
import com.iceshardgames.gamercommunity.DB.ChatMessage;
import com.iceshardgames.gamercommunity.DB.ChatUser;
import com.iceshardgames.gamercommunity.DB.ChatUserDao;
import com.iceshardgames.gamercommunity.Model.Request.ConversationCreateRequest;
import com.iceshardgames.gamercommunity.Model.Response.ConversationResponse;
import com.iceshardgames.gamercommunity.Model.DeviceInfo;
import com.iceshardgames.gamercommunity.Model.Request.DeviceRegistrationRequest;
import com.iceshardgames.gamercommunity.Model.Response.DevicesResponse;
import com.iceshardgames.gamercommunity.Model.EncryptedPayload;
import com.iceshardgames.gamercommunity.Model.Request.LastSeenRequest;
import com.iceshardgames.gamercommunity.Model.Response.LastSeenResponse;
import com.iceshardgames.gamercommunity.Model.MessageDto;
import com.iceshardgames.gamercommunity.Model.Response.MessagesResponse;
import com.iceshardgames.gamercommunity.Model.Request.SendMessageRequest;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.SessionManager;
import com.iceshardgames.gamercommunity.Utills.Utills;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.TimeZone;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatDetailActivity extends AppCompatActivity {

    private String otherUserId, conversationId, myUserId;
    private String localChatId; // <<< CRITICAL: used for local DB linkage
    private RecyclerView rv;
    private ChatMessageAdapter adapter;
    private EditText et;
    private ImageView btnSend;
    private TextView title;

    private ApiService api;
    private ChatSocketManager socket;
    private DeviceManager deviceManager;
    private ChatSocketManager.Listener socketListener;

    // Local DB (per logged in user)
    private AppDatabase db;
    private ExecutorService io = Executors.newSingleThreadExecutor();
    private String partnerName;
    private TextView tvLastSeen;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_detail);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Utills.showLoadingDialog(ChatDetailActivity.this);

        // ---- Intent data
        otherUserId = getIntent().getStringExtra("other_user_id");
        partnerName = getIntent().getStringExtra("chat_partner_name");
        final String explicitChatId = getIntent().getStringExtra("chat_id");
        tvLastSeen = findViewById(R.id.last_seen);

        // ---- Views
        rv = findViewById(R.id.rvMessages);
        LinearLayoutManager lm = new LinearLayoutManager(this);
        lm.setStackFromEnd(true);
        rv.setLayoutManager(lm);
        et = findViewById(R.id.etMessage);
        btnSend = findViewById(R.id.btnSend);
        title = findViewById(R.id.title);
        title.setText(partnerName != null ? partnerName : "Chat");

        // ---- API / socket infra
        api = ApiClient.getRetrofit().create(ApiService.class);
        deviceManager = new DeviceManager(this);
        socket = new ChatSocketManager("https://forum-sjpj.onrender.com");

        // ---- Session
        SharedPreferences up = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        final String token = "Bearer " + up.getString("accessToken", "");
        myUserId = up.getString("userId", ""); // ensure set at login

        // ---- Local DB instance bound to current user
        String currentUserId = SessionManager.getUserId(this);
        if (currentUserId == null || currentUserId.isEmpty()) currentUserId = "guest";
        db = AppDatabase.getInstance(getApplicationContext(), currentUserId);

        // ---- Decide the local chat id used by Room
        // Prefer an explicit chat_id passed from list. Otherwise use a stable DM id.
        localChatId = (explicitChatId != null && !explicitChatId.isEmpty())
                ? explicitChatId
                : ("dm_" + (otherUserId != null ? otherUserId : "unknown"));

        // Ensure a ChatUser row exists so this chat shows up in the list
        ensureChatUserRow(localChatId, partnerName);

        // ---- Adapter
        adapter = new ChatMessageAdapter(myUserId, partnerName, this);
        rv.setAdapter(adapter);
        rv.setHasFixedSize(true);
        rv.setItemViewCacheSize(20);
        rv.setItemAnimator(null);

        // ---- ONE socket listener only
        socketListener = new ChatSocketManager.Listener() {
            @Override
            public void onNewMessage(String cId, String msgId, String senderId, String text, String createdAtIso) {
                if (conversationId == null || !cId.equals(conversationId)) return;

                long ts;
                try {
                    ts = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                            ? java.time.Instant.parse(createdAtIso).toEpochMilli()
                            : System.currentTimeMillis();
                } catch (Exception e) {
                    ts = System.currentTimeMillis();
                }

                final boolean isMine = myUserId.equals(senderId);

                long finalTs = ts;
                runOnUiThread(() -> {
                    if (isMine) {
                        // ACK for my own message → update optimistic bubble
                        adapter.ackLastOutgoingFromMe(finalTs, msgId);
                    } else {
                        // Incoming message → add with server timestamp
                        MessageDto m = new MessageDto();
                        m.setId(msgId);
                        m.setSender(senderId);
                        m.setText(text);
                        m.setSentAt(finalTs);
                        adapter.add(m);
                        boolean atBottom = !rv.canScrollVertically(1);
                        if (atBottom) rv.scrollToPosition(adapter.getItemCount() - 1);
                    }
                });

                // Persist to local DB for last message preview (both mine and incoming)
                saveLocalMessage(text, ts, isMine);

                // Delivery / read
                if (!isMine) {
                    markDeliveredAsync(token, msgId);
                    // Mark read if user is already at bottom (simple heuristic)
                    if (!rv.canScrollVertically(1)) markReadAsync(token, msgId);
                }
            }

            @Override public void onDelivered(String messageId) { }
            @Override public void onRead(String messageId) { }
            @Override public void onTyping(String c, String u, boolean isTyping) { }
        };

        // ---- Connect socket ONCE (after myUserId/token known)
        socket.connect(myUserId, deviceManager.getOrCreateDeviceId(), token, socketListener);

        // ---- Mark read when scrolled to bottom
        rv.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (!recyclerView.canScrollVertically(1)) {
                    markAllReadVisible(token);
                }
            }
        });

        // ---- Bootstrap: register device → conversation → history
        registerDevice(token, () ->
                createOrGetConversation(token, otherUserId, convId -> {
                    conversationId = convId;
                    fetchConversationMeta(token, convId);
                    loadMessages(token, convId); // will backfill local DB too
                })
        );

        // ---- Send
        btnSend.setOnClickListener(v -> {
            String text = et.getText().toString().trim();
            if (text.isEmpty() || conversationId == null) return;
            sendMessage(token, conversationId, text);
            et.setText("");
        });

        // update on entry
        updateLastSeenAndRender();
    }

    private void registerDevice(String token, Runnable then) {
        DeviceRegistrationRequest body = new DeviceRegistrationRequest(
                deviceManager.getOrCreateDeviceId(),
                deviceManager.getPublicKey(),
                deviceManager.getDeviceName(),
                "android"
        );
        api.registerDevice(token, body).enqueue(new Callback<DevicesResponse>() {
            @Override public void onResponse(Call<DevicesResponse> call, Response<DevicesResponse> r) { then.run(); }
            @Override public void onFailure(Call<DevicesResponse> call, Throwable t) { then.run(); }
        });
    }

    private void createOrGetConversation(String token, String otherUserId, java.util.function.Consumer<String> cb) {
        api.createOrGetConversation(token, new ConversationCreateRequest(otherUserId))
                .enqueue(new Callback<ConversationResponse>() {
                    @Override
                    public void onResponse(Call<ConversationResponse> call, Response<ConversationResponse> r) {
                        if (r.isSuccessful() && r.body() != null && r.body().getData() != null) {
                            cb.accept(r.body().getData().getConversationId());
                        } else cb.accept(null);
                    }
                    @Override public void onFailure(Call<ConversationResponse> call, Throwable t) { cb.accept(null); }
                });
    }

    private void fetchConversationMeta(String token, String convId) {
        api.getConversation(token, convId).enqueue(new Callback<ConversationResponse>() {
            @Override
            public void onResponse(Call<ConversationResponse> call, Response<ConversationResponse> r) {
                Utills.hideLoadingDialog();
                // Optionally set title/avatar from r.body()
            }
            @Override public void onFailure(Call<ConversationResponse> call, Throwable t) {
                Utills.hideLoadingDialog();
            }
        });
    }

    private void loadMessages(String token, String convId) {
        api.getMessages(token, convId, 1, 20).enqueue(new Callback<MessagesResponse>() {
            @Override
            public void onResponse(Call<MessagesResponse> call, Response<MessagesResponse> r) {
                if (!r.isSuccessful()) return;
                MessagesResponse body = r.body();
                if (body == null || body.getData() == null || body.getData().getMessages() == null) return;

                List<MessagesResponse.MessageNet> nets = body.getData().getMessages();

                io.execute(() -> {
                    List<MessageDto> ui = new ArrayList<>(nets.size());
                    for (MessagesResponse.MessageNet n : nets) {
                        MessageDto m = toUi(n);
                        ui.add(m);

                        // Persist history to local DB
                        final long ts = m.getSentAt() > 0 ? m.getSentAt() : System.currentTimeMillis();
                        final boolean isMine = myUserId.equals(m.getSender());
                        saveLocalMessage(n.getPayloads() != null && !n.getPayloads().isEmpty()
                                        ? n.getPayloads().get(0).getCiphertext()
                                        : "",
                                ts, isMine);
                    }

                    runOnUiThread(() -> {
                        adapter.setAll(ui);
                        if (adapter.getItemCount() > 0)
                            rv.scrollToPosition(adapter.getItemCount() - 1);
                    });
                });
            }
            @Override public void onFailure(Call<MessagesResponse> call, Throwable t) { }
        });
    }

    private static MessageDto toUi(MessagesResponse.MessageNet net) {
        final String senderId = net.getSender() != null ? net.getSender().getId() : "";
        final String text = (net.getPayloads() != null && !net.getPayloads().isEmpty())
                ? net.getPayloads().get(0).getCiphertext()
                : "";

        MessageDto m = new MessageDto();
        m.setId(net.getMessageId());
        m.setSender(senderId);
        m.setText(text);
        // createdAt ISO → millis
        try {
            long ts = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                    ? java.time.Instant.parse(net.getCreatedAt()).toEpochMilli()
                    : System.currentTimeMillis();
            m.setSentAt(ts);
        } catch (Exception e) {
            m.setSentAt(System.currentTimeMillis());
        }
        return m;
    }

    private void sendMessage(String token, String convId, String text) {
        final String myDeviceId = deviceManager.getOrCreateDeviceId();

        api.getDevicesForUser(token, otherUserId).enqueue(new Callback<DevicesResponse>() {
            @Override
            public void onResponse(Call<DevicesResponse> call, Response<DevicesResponse> r) {
                int MAX_DEVICES = 2;
                Set<String> seen = new LinkedHashSet<>();
                List<EncryptedPayload> payloads = new ArrayList<>();
                List<DeviceInfo> devices = (r.isSuccessful() && r.body() != null && r.body().getData() != null)
                        ? r.body().getData().getDevices()
                        : Collections.emptyList();

                for (DeviceInfo d : devices) {
                    if (d == null) continue;
                    if (myDeviceId.equals(d.getDeviceId())) continue;
                    if (seen.add(d.getDeviceId())) {
                        payloads.add(new EncryptedPayload(d.getDeviceId(), text));
                        if (payloads.size() >= MAX_DEVICES) break;
                    }
                }

                if (payloads.isEmpty()) {
                    Toast.makeText(ChatDetailActivity.this,
                            "Sent, but recipient hasn’t registered a device yet.",
                            Toast.LENGTH_SHORT).show();
                }

                api.sendMessage(token, convId, new SendMessageRequest(payloads, "text", null))
                        .enqueue(new Callback<MessagesResponse>() {
                            @Override public void onResponse(Call<MessagesResponse> call, Response<MessagesResponse> rr) { }
                            @Override public void onFailure(Call<MessagesResponse> call, Throwable t) { }
                        });

                // Optimistic UI add with provisional local timestamp
                long now = System.currentTimeMillis();
                MessageDto m = new MessageDto();
                m.setId(UUID.randomUUID().toString()); // temp id
                m.setSender(myUserId);
                m.setText(text);
                m.setSentAt(now);
                adapter.add(m);
                rv.scrollToPosition(adapter.getItemCount() - 1);

                // Persist my outgoing message immediately to local DB
                saveLocalMessage(text, now, true);
            }

            @Override public void onFailure(Call<DevicesResponse> call, Throwable t) { }
        });
    }

    private void markAllReadVisible(String token) {
        if (adapter == null) return;
        List<MessageDto> items = adapter.getItems();
        if (items == null) return;
        for (MessageDto m : items) {
            if (m == null) continue;
            if (m.getSender() != null && !m.getSender().equals(myUserId)) {
                markReadAsync(token, m.getId());
            }
        }
    }

    private void markReadAsync(String token, String messageId) {
        api.markRead(token, messageId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> r) { }
            @Override public void onFailure(Call<Void> call, Throwable t) { }
        });
    }

    private void markDeliveredAsync(String token, String messageId) {
        api.markDelivered(token, messageId).enqueue(new Callback<Void>() {
            @Override public void onResponse(Call<Void> call, Response<Void> r) { }
            @Override public void onFailure(Call<Void> call, Throwable t) { }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (socket != null) socket.disconnect();
        io.shutdown();
    }

    // -------------------------
    // Helpers for local storage
    // -------------------------

    private void saveLocalMessage(String text, long timestamp, boolean isMine) {
        io.execute(() -> {
            try {
                ChatMessage cm = new ChatMessage();
                cm.setId(java.util.UUID.randomUUID().toString()); // ✅ ensure PK set
                cm.setChatId(localChatId);                        // ✅ MUST match list chatId
                cm.setMessage(text);
                cm.setTimestamp(timestamp);
                try { cm.setSentByMe(isMine); } catch (Throwable ignored) { }
                db.chatMessageDao().insert(cm);

// ✅ update last message for chat list (same behavior as ChatDetailActivity1)
                upsertChatUserLast(text, timestamp);
                // Optionally, update ChatUser's lastActive for sort/pin logic
            } catch (Throwable t) {
                Log.e("ChatDetailActivity", "saveLocalMessage failed", t);
            }
        });
    }

    private void ensureChatUserRow(String chatId, String name) {
        io.execute(() -> {
            try {
                ChatUserDao dao = db.chatUserDao();
                ChatUser existing = dao.getUserByChatId(chatId);
                if (existing == null) {
                    ChatUser u = new ChatUser(
                            chatId,
                            (name != null && !name.isEmpty()) ? name : "User",
                            "", // maybe store otherUserId here if you like
                            "", // avatar url
                            System.currentTimeMillis(),
                            false,
                            false,
                            R.drawable.profilepic
                    );
                    dao.insertOrUpdate(u);
                }
            } catch (Throwable t) {
                Log.e("ChatDetailActivity", "ensureChatUserRow failed", t);
            }
        });
    }

    private void upsertChatUserLast(String text, long ts) {
        io.execute(() -> {
            try {
                ChatUserDao cud = db.chatUserDao();
                ChatUser u = cud.getUserByChatId(localChatId);

                String timeStr = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault())
                        .format(new java.util.Date(ts));

                if (u == null) {
                    u = new ChatUser(
                            localChatId,
                            (partnerName != null && !partnerName.isEmpty()) ? partnerName : "User",
                            text,               // lastMessage
                            timeStr,            // lastMessageTime
                            ts,                 // timestamp for sorting
                            false,
                            false,
                            R.drawable.profilepic
                    );
                } else {
                    u.setLastMessage(text);
                    u.setLastMessageTime(timeStr);
                    u.setTimestamp(ts);
                }
                cud.insertOrUpdate(u);
            } catch (Throwable ignored) { }
        });
    }

    private void updateLastSeenAndRender() {
        SharedPreferences sp = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        String tokenRaw = sp.getString("accessToken", "");
        if (tokenRaw == null || tokenRaw.isEmpty()) {
            tvLastSeen.setText("Last seen — unknown");
            return;
        }

        String deviceId = sp.getString("deviceId_server", null);
        if (deviceId == null || deviceId.isEmpty()) {
            // fallback if not saved yet
            DeviceManager dm = new DeviceManager(getApplicationContext());
            deviceId = dm.getOrCreateDeviceId();
        }

        String bearer = "Bearer " + tokenRaw;
        api.updateLastSeen(bearer, new LastSeenRequest(deviceId))
                .enqueue(new retrofit2.Callback<LastSeenResponse>() {
                    @Override public void onResponse(Call<LastSeenResponse> call, Response<LastSeenResponse> r) {
                        if (!r.isSuccessful() || r.body() == null || r.body().timestamp == null) {
                            tvLastSeen.setText("Last seen — just now");
                            return;
                        }
                        String iso = r.body().timestamp; // e.g. 2025-08-28T04:36:50.248Z
                        tvLastSeen.setText("Last seen " + toFriendlyLastSeen(iso));
                    }

                    @Override public void onFailure(Call<LastSeenResponse> call, Throwable t) {
                        tvLastSeen.setText("Last seen — unknown");
                        Log.e("==namah", "last-seen error", t);
                    }
                });
    }

    private String toFriendlyLastSeen(String isoUtc) {
        try {
            long ms = parseIsoToMillis(isoUtc);
            Date date = new Date(ms);

            // check if it's today
            java.util.Calendar cal1 = java.util.Calendar.getInstance();
            java.util.Calendar cal2 = java.util.Calendar.getInstance();
            cal2.setTime(date);

            boolean sameDay = cal1.get(java.util.Calendar.YEAR) == cal2.get(java.util.Calendar.YEAR)
                    && cal1.get(java.util.Calendar.DAY_OF_YEAR) == cal2.get(java.util.Calendar.DAY_OF_YEAR);

            SimpleDateFormat fmt = new SimpleDateFormat("h:mm a", Locale.getDefault());
            fmt.setTimeZone(TimeZone.getDefault());
            String timePart = fmt.format(date);

            if (sameDay) {
                return "today at " + timePart;
            } else {
                SimpleDateFormat fullFmt = new SimpleDateFormat("MMM d 'at' h:mm a", Locale.getDefault());
                fullFmt.setTimeZone(TimeZone.getDefault());
                return "Last seen " + fullFmt.format(date);
            }
        } catch (Exception e) {
            Log.w("==namah", "parse last-seen failed, raw=" + isoUtc, e);
            return "Last seen —";
        }
    }

    private long parseIsoToMillis(String isoUtc) throws ParseException {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            return Instant.parse(isoUtc).toEpochMilli();
        } else {
            // tolerate fractional seconds or plain Z
            String pattern = isoUtc.contains(".")
                    ? "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
                    : "yyyy-MM-dd'T'HH:mm:ss'Z'";
            SimpleDateFormat sdf = new SimpleDateFormat(pattern, Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = sdf.parse(isoUtc);
            return d != null ? d.getTime() : System.currentTimeMillis();
        }
    }

    private String exactLocalTime(String isoUtc) {
        // Asia/Kolkata as you’re in India
        String zone = "Asia/Kolkata";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            ZonedDateTime zdt = Instant.parse(isoUtc).atZone(ZoneId.of(zone));
            DateTimeFormatter fmt = DateTimeFormatter.ofPattern("MMM d, h:mm a", Locale.getDefault());
            return zdt.format(fmt);
        } else {
            try {
                long ms = parseIsoToMillis(isoUtc);
                SimpleDateFormat fmt = new SimpleDateFormat("MMM d, h:mm a", Locale.getDefault());
                fmt.setTimeZone(TimeZone.getTimeZone(zone));
                return fmt.format(new Date(ms));
            } catch (Exception e) {
                return "";
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        // optional: refresh each time user comes back
        updateLastSeenAndRender();
    }
}
