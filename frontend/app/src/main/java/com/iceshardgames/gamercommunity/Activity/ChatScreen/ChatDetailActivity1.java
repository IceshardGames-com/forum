package com.iceshardgames.gamercommunity.Activity.ChatScreen;

import android.Manifest;
import android.content.ContentValues;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.DragEvent;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.iceshardgames.gamercommunity.Adapter.MessageAdapter;
import com.iceshardgames.gamercommunity.DB.AppDatabase;
import com.iceshardgames.gamercommunity.DB.ChatMessage;
import com.iceshardgames.gamercommunity.DB.ChatMessageDao;
import com.iceshardgames.gamercommunity.DB.ChatUser;
import com.iceshardgames.gamercommunity.DB.ChatUserDao;
import com.iceshardgames.gamercommunity.Model.MessageItem;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.SessionManager;
import com.iceshardgames.gamercommunity.Utills.Utills;
import com.iceshardgames.gamercommunity.databinding.ActivityChatDetailBinding;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatDetailActivity1 extends AppCompatActivity {

    ActivityChatDetailBinding binding;
    private MessageAdapter messageAdapter;
    private List<MessageItem> messageList;
    private AppDatabase db;
    private String chatPartnerName;

    private ChatMessageDao dao;
    private ChatUserDao userDao;
    private String chatId;
    private final ExecutorService executor = Executors.newSingleThreadExecutor();
    private final Handler uiHandler = new Handler(Looper.getMainLooper());
    private static final int CAPTURE_IMAGE_REQUEST = 1002;
    private Uri cameraImageUri;
    private static final int CAMERA_PERMISSION_REQUEST = 101;

    private final ActivityResultLauncher<Intent> filePickerLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    handleSelectedFiles(result.getData());
                }
            }
    );

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatDetailBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // ✅ Set user data
        chatPartnerName = getIntent().getStringExtra("chat_partner_name");
        Log.e("==sana", "resu: "+chatPartnerName );
        chatId = getIntent().getStringExtra("chat_id");
//        if (chatId == null) chatId = "default_chat"; // fallback

        binding.chatUsername.setText(chatPartnerName);
        binding.avatarDetail.setImageResource(R.drawable.profilepic); // or pass from intent

        // ✅ Init database and DAO
        String currentUserId = SessionManager.getUserId(this); // fetch logged-in user id
        if (currentUserId == null) currentUserId = "guest";   // fallback if not logged in
        db = AppDatabase.getInstance(this, currentUserId);
        dao = db.chatMessageDao();
        userDao = db.chatUserDao();

        // ✅ Set up RecyclerView and Adapter
        messageList = new ArrayList<>(); // ✅ ADD THIS LINE
        messageAdapter = new MessageAdapter(messageList);
        binding.messagesRecycler.setLayoutManager(new LinearLayoutManager(this));
        binding.messagesRecycler.setAdapter(messageAdapter);
        binding.messageInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus) {
                binding.messagesRecycler.postDelayed(() -> {
                    binding.messagesRecycler.scrollToPosition(messageAdapter.getItemCount() - 1);
                }, 150);
            }
        });
        loadChatMessages();

        binding.sendButton.setOnClickListener(v -> sendTextMessage());

        binding.attachButton.setOnClickListener(v -> pickFiles());

        binding.cameraButton.setOnClickListener(v -> captureImage());

        binding.messagesRecycler.setOnDragListener((view, dragEvent) -> {
            if (dragEvent.getAction() == DragEvent.ACTION_DROP) {
                Uri uri = dragEvent.getClipData().getItemAt(0).getUri();
                handleDroppedFile(uri);
                return true;
            }
            return true;
        });

    }

    private void pickFiles() {
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("*/*");
        String[] mimeTypes = {"image/*", "video/*", "application/pdf", "text/plain"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        filePickerLauncher.launch(intent);
    }

    private void requestCameraPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.CAMERA},
                    CAMERA_PERMISSION_REQUEST);
        } else {
            openCamera(); // permission already granted
        }
    }

    private void openCamera() {
        ContentValues values = new ContentValues();
        values.put(MediaStore.Images.Media.TITLE, "NewPic_" + System.currentTimeMillis());
        cameraImageUri = getContentResolver().insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);

        if (cameraImageUri != null) {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, cameraImageUri);
            startActivityForResult(intent, CAPTURE_IMAGE_REQUEST);
        } else {
            Toast.makeText(this, "Failed to create media URI", Toast.LENGTH_SHORT).show();
        }
    }
    private void handleSelectedFiles(Intent data) {
        if (data.getClipData() != null) {
            for (int i = 0; i < data.getClipData().getItemCount(); i++) {
                Uri fileUri = data.getClipData().getItemAt(i).getUri();
                handleFile(fileUri);
            }
        } else {
            Uri fileUri = data.getData();
            if (fileUri != null) handleFile(fileUri);
        }
    }

    private void handleDroppedFile(Uri uri) {
        handleFile(uri);
    }

    private void captureImage() {
        requestCameraPermission();
    }

    private void handleFile(Uri uri) {
        String fileName = getFileName(uri);
        String mimeType = getContentResolver().getType(uri);
        String emoji = "📎";
        long time = System.currentTimeMillis();

        if (mimeType != null) {
            if (mimeType.startsWith("image/")) emoji = "🖼️";
            else if (mimeType.startsWith("video/")) emoji = "🎬";
            else if (mimeType.equals("application/pdf")) emoji = "📄";
        }

        String display = emoji + " " + fileName;
        String formattedTime = getFormattedTime(time);
        String messageId = UUID.randomUUID().toString(); // Generate unique ID

        // 🔥 Create Room entity
        MessageItem messageItem = new MessageItem(messageId,display, true, formattedTime, Utills.getUsername(this));
        messageItem.setMediaUri(uri.toString());
        messageItem.setFileType(mimeType);

        messageList.add(messageItem);
        messageAdapter.notifyItemInserted(messageList.size() - 1);
        binding.messagesRecycler.scrollToPosition(messageList.size() - 1);

        executor.execute(() -> {
            ChatMessage msg = new ChatMessage(chatId, display, true, time);
            msg.setFileUri(uri.toString());
            msg.setFileType(mimeType);
            dao.insert(msg);

            ChatUser user = new ChatUser(chatId, chatPartnerName, display, formattedTime, time, false, false, R.drawable.profilepic);
            userDao.insertOrUpdate(user);
        });
    }

    private void sendTextMessage() {
        String text = binding.messageInput.getText().toString().trim();
        if (text.isEmpty()) return;

        long time = System.currentTimeMillis();
        String formattedTime = getFormattedTime(time);

        ChatMessage msg = new ChatMessage(chatId, text, true, time);
        String currentUser = Utills.getUsername(ChatDetailActivity1.this);
        String chatPartner = chatPartnerName; // Person you're chatting with

        executor.execute(() -> {
            dao.insert(msg);
            // ✅ Don't overwrite username — use chat partner's name
            Log.e("==sana", "sendTextMessage: "+chatPartner );
            userDao.insertOrUpdate(new ChatUser(chatId, chatPartner, text, formattedTime, time, false, false, R.drawable.profilepic));

            uiHandler.post(() -> {
                messageList.add(new MessageItem(msg.getChatId(),text, true, formattedTime, currentUser));
                messageAdapter.notifyItemInserted(messageList.size() - 1);
                binding.messageInput.setText("");
                binding.messagesRecycler.scrollToPosition(messageList.size() - 1);
            });

        });

        binding.messageInput.setOnFocusChangeListener((v, hasFocus) -> {
            if (hasFocus && messageAdapter != null) {
                binding.messagesRecycler.postDelayed(() -> {
                    binding.messagesRecycler.scrollToPosition(messageAdapter.getItemCount() - 1);
                }, 100);
            }
        });
    }

    private void loadChatMessages() {
        executor.execute(() -> {
            List<ChatMessage> messages = dao.getMessagesForChat(chatId);
            messageList.clear();

            for (ChatMessage m : messages) {
                String formattedTime = getFormattedTime(m.getTimestamp());

                // If fileUri exists, show media preview
                if (m.getFileUri() != null && !m.getFileUri().isEmpty()) {
                    messageList.add(new MessageItem(
                            m.getChatId(),
                            m.getMessage(),
                            m.isSentByUser(),
                            formattedTime,
                            Utills.getUsername(this),
                            m.getFileUri(),
                            m.getFileType()
                    ));
                } else {
                    messageList.add(new MessageItem(
                            m.getChatId(),
                            m.getMessage(),
                            m.isSentByUser(),
                            formattedTime,
                            Utills.getUsername(this)
                    ));
                }
            }

            runOnUiThread(() -> {
                messageAdapter.notifyDataSetChanged();
                binding.messagesRecycler.scrollToPosition(messageList.size() - 1);
            });
        });

    }

    private String getFormattedTime(long millis) {
        return new SimpleDateFormat("hh:mm a", Locale.getDefault()).format(new Date(millis));
    }

    private String getFileName(Uri uri) {
        String result = null;
        if ("content".equals(uri.getScheme())) {
            try (Cursor c = getContentResolver().query(uri, null, null, null, null)) {
                if (c != null && c.moveToFirst()) {
                    result = c.getString(c.getColumnIndex(OpenableColumns.DISPLAY_NAME));
                }
            }
        }
        return result != null ? result : uri.getLastPathSegment();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == RESULT_OK) {
            if (cameraImageUri != null) {
                handleFile(cameraImageUri);
                Toast.makeText(this, "Image captured!", Toast.LENGTH_SHORT).show();
            }else {
                Toast.makeText(this, "Image URI was null", Toast.LENGTH_SHORT).show();
            }
        }
    }
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                captureImage();
            } else {
                Toast.makeText(this, "Camera permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

}