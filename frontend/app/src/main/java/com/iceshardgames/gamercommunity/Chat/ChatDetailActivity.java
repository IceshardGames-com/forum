package com.iceshardgames.gamercommunity.Chat;

import static android.content.Context.MODE_PRIVATE;

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
import com.iceshardgames.gamercommunity.Model.ConversationCreateRequest;
import com.iceshardgames.gamercommunity.Model.ConversationResponse;
import com.iceshardgames.gamercommunity.Model.DeviceInfo;
import com.iceshardgames.gamercommunity.Model.DeviceRegistrationRequest;
import com.iceshardgames.gamercommunity.Model.DevicesResponse;
import com.iceshardgames.gamercommunity.Model.EncryptedPayload;
import com.iceshardgames.gamercommunity.Model.MessageDto;
import com.iceshardgames.gamercommunity.Model.MessagesResponse;
import com.iceshardgames.gamercommunity.Model.SendMessageRequest;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatDetailActivity extends AppCompatActivity {

    private String otherUserId, conversationId, myUserId;
    private RecyclerView rv;
    private ChatMessageAdapter adapter;
    private EditText et;
    private ImageView btnSend;
    private TextView title;

    private ApiService api;
    private ChatSocketManager socket;
    private DeviceManager deviceManager;
    private ChatSocketManager.Listener socketListener;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_chat_detail2);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        Utills.showLoadingDialog(ChatDetailActivity.this);

        // ---- Intent data
        otherUserId = getIntent().getStringExtra("other_user_id");
        String partnerName = getIntent().getStringExtra("chat_partner_name");

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

                runOnUiThread(() -> {
                    if (myUserId.equals(senderId)) {
                        // ACK for my own message → update the last optimistic bubble
                        long ts;
                        try {
                            ts = (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                                    ? java.time.Instant.parse(createdAtIso).toEpochMilli()
                                    : System.currentTimeMillis();
                        } catch (Exception e) {
                            ts = System.currentTimeMillis();
                        }
                        adapter.ackLastOutgoingFromMe(ts, msgId);
                    } else {
                        // Incoming message → add with server timestamp
                        MessageDto m = new MessageDto();
                        m.setId(msgId);
                        m.setSender(senderId);
                        m.setText(text);
                        m.setSentAtFromIso(createdAtIso);
                        adapter.add(m);
                        boolean atBottom = !rv.canScrollVertically(1);
                        if (atBottom) rv.scrollToPosition(adapter.getItemCount() - 1);

                        markDeliveredAsync(token, msgId);
                        if (atBottom) markReadAsync(token, msgId);
                    }
                });
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
                    loadMessages(token, convId);
                })
        );

        // ---- Send
        btnSend.setOnClickListener(v -> {
            String text = et.getText().toString().trim();
            if (text.isEmpty() || conversationId == null) return;
            sendMessage(token, conversationId, text);
            et.setText("");
        });
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
                new Thread(() -> {
                    List<MessageDto> ui = new ArrayList<>(nets.size());
                    for (MessagesResponse.MessageNet m : nets) ui.add(toUi(m));

                    runOnUiThread(() -> {
                        adapter.setAll(ui);
                        if (adapter.getItemCount() > 0)
                            rv.scrollToPosition(adapter.getItemCount() - 1);
                    });
                }).start();
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
        m.setSentAtFromIso(net.getCreatedAt()); // CRITICAL
        // If you have media/type, set here too
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

                // Optimistic add with provisional local timestamp
                MessageDto m = new MessageDto();
                m.setId(UUID.randomUUID().toString()); // temp id
                m.setSender(myUserId);
                m.setText(text);
                m.setSentAt(System.currentTimeMillis()); // show time immediately
                adapter.add(m);
                rv.scrollToPosition(adapter.getItemCount() - 1);
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
    }
}
