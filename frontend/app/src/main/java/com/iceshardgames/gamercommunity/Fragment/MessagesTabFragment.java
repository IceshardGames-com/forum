package com.iceshardgames.gamercommunity.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.iceshardgames.gamercommunity.APIintegration.ApiClient;
import com.iceshardgames.gamercommunity.APIintegration.ApiService;
import com.iceshardgames.gamercommunity.Adapter.ChannelAdapter;
import com.iceshardgames.gamercommunity.Adapter.ChatAdapter;
import com.iceshardgames.gamercommunity.Adapter.UserSearchAdapter;
import com.iceshardgames.gamercommunity.DB.AppDatabase;
import com.iceshardgames.gamercommunity.DB.Channel;
import com.iceshardgames.gamercommunity.DB.ChatMessage;
import com.iceshardgames.gamercommunity.DB.ChatUser;
import com.iceshardgames.gamercommunity.Model.ChatItem;
import com.iceshardgames.gamercommunity.Model.Request.SendFriendRequest;
import com.iceshardgames.gamercommunity.Model.Response.SendFriendRequestResponse;
import com.iceshardgames.gamercommunity.Model.Response.UserSearchResponse;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.SessionManager;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MessagesTabFragment extends Fragment {

    private RecyclerView chatListRecycler, searchResultRecycler, exploreChannelsRecycler;
    private View searchMain, ivClear;
    private EditText searchChat;

    private final List<ChatItem> chatList = new ArrayList<>();
    private ChatAdapter chatAdapter;

    private final ItemTouchHelper.SimpleCallback swipeCallback =
            new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {
                @Override public boolean onMove(@NonNull RecyclerView rv, @NonNull RecyclerView.ViewHolder vh, @NonNull RecyclerView.ViewHolder t) { return false; }
                @Override public void onSwiped(@NonNull RecyclerView.ViewHolder vh, int dir) {
                    int position = vh.getAdapterPosition();
                    ChatItem chat = chatList.get(position);
                    if (dir == ItemTouchHelper.LEFT) {
                        chat.setMuted(!chat.isMuted());
                        Toast.makeText(getActivity(), chat.isMuted() ? "Muted" : "Unmuted", Toast.LENGTH_SHORT).show();
                    } else if (dir == ItemTouchHelper.RIGHT) {
                        chat.setPinned(!chat.isPinned());
                        Toast.makeText(getActivity(), chat.isPinned() ? "Pinned" : "Unpinned", Toast.LENGTH_SHORT).show();
                    }
                    Collections.sort(chatList, (c1, c2) -> Boolean.compare(c2.isPinned(), c1.isPinned()));
                    chatAdapter.notifyDataSetChanged();
                }
            };

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_messages, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        chatListRecycler = v.findViewById(R.id.chat_list_recycler);
        searchResultRecycler = v.findViewById(R.id.searchResultRecycler);
        exploreChannelsRecycler = v.findViewById(R.id.explore_channels_recycler);
        searchMain = v.findViewById(R.id.search_chatmain);
        searchChat = v.findViewById(R.id.search_chat);
        ivClear = v.findViewById(R.id.iv_clear);

        chatListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        searchResultRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        exploreChannelsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        chatAdapter = new ChatAdapter(getContext(), chatList);
        chatListRecycler.setAdapter(chatAdapter);
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(chatListRecycler);

        // FAB click from parent
//        getParentFragmentManager().setFragmentResultListener("fab_click", this, (reqKey, bundle) -> showNewChatDialog());

        setupSearch();
        loadExploreChannels();
        loadRecentChatUsers();
    }

    private void setupSearch() {
        searchChat.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {}
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                if (s.length() > 1) {
                    searchUsersApi(s.toString());
                } else if (s.length() == 0) {
                    searchResultRecycler.setVisibility(View.GONE);
                    chatListRecycler.setVisibility(View.VISIBLE);
                }
            }
            @Override public void afterTextChanged(Editable s) {
                ivClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }
        });

        ivClear.setOnClickListener(v -> {
            searchChat.setText("");
            searchResultRecycler.setVisibility(View.GONE);
            chatListRecycler.setVisibility(View.VISIBLE);
        });
    }

    // ======= Your existing methods, adapted to local views =======

    private void searchUsersApi(String query) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);
        String token = "Bearer " + accessToken;

        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        apiService.searchUsers(token, query, 1, 20, false).enqueue(new Callback<UserSearchResponse>() {
            @Override
            public void onResponse(Call<UserSearchResponse> call, Response<UserSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserSearchResponse.User> users = response.body().getData().getUsers();

                    UserSearchAdapter adapter = new UserSearchAdapter(getContext(), users, user -> {
                        Toast.makeText(getContext(), "Clicked: " + user.getUsername(), Toast.LENGTH_SHORT).show();
                        sendFriendRequest(user.getId());
                    });

                    searchResultRecycler.setAdapter(adapter);
                    chatListRecycler.setVisibility(View.GONE);
                    searchResultRecycler.setVisibility(View.VISIBLE);
                } else {
                    Toast.makeText(getContext(), "No results", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserSearchResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void sendFriendRequest(String userId) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);
        String token = "Bearer " + accessToken;
        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);

        apiService.sendFriendRequest(token, new SendFriendRequest(userId)).enqueue(new Callback<SendFriendRequestResponse>() {
            @Override
            public void onResponse(Call<SendFriendRequestResponse> call, Response<SendFriendRequestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "✅ " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    // restore messages view
                    searchResultRecycler.setVisibility(View.GONE);
                    chatListRecycler.setVisibility(View.VISIBLE);
                } else {
                    try {
                        String err = response.errorBody() != null ? response.errorBody().string() : "Unknown";
                        Toast.makeText(getContext(), "❌ " + err, Toast.LENGTH_LONG).show();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

            @Override
            public void onFailure(Call<SendFriendRequestResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadExploreChannels() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String currentUserId = SessionManager.getUserId(requireContext());
            if (currentUserId == null) currentUserId = "guest";

            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext(), currentUserId);

            if (db.channelDao().getAllChannels().isEmpty()) {
                db.channelDao().insert(new Channel("1", "FPS Gaming", 234, R.drawable.profilepic, false));
                db.channelDao().insert(new Channel("2", "MMORPG Fans", 120, R.drawable.profilepic, true));
                db.channelDao().insert(new Channel("3", "Indie Games", 85, R.drawable.profilepic, false));
            }

            List<Channel> channelList = db.channelDao().getAllChannels();

            requireActivity().runOnUiThread(() -> {
                ChannelAdapter adapter = new ChannelAdapter(getContext(), channelList);
                exploreChannelsRecycler.setAdapter(adapter);
            });
        });
    }

    private void loadRecentChatUsers() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String currentUserId = SessionManager.getUserId(requireContext());
            if (currentUserId == null) currentUserId = "guest";

            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext(), currentUserId);
            List<ChatUser> chatUsers = db.chatUserDao().getAllUsers();
            List<ChatItem> tempChatList = new ArrayList<>();

            for (ChatUser user : chatUsers) {
                ChatMessage lastMsg = db.chatMessageDao().getLastMessageForChat(user.getChatId());
                String lastMessageText = (lastMsg != null && lastMsg.getMessage() != null) ? lastMsg.getMessage() : "";
                String lastMessageTime = lastMsg != null ? formatTimestamp(lastMsg.getTimestamp()) : "";

                ChatItem item = new ChatItem(
                        user.getChatId(),
                        user.getUserName(),
                        lastMessageText,
                        lastMessageTime,
                        user.getAvatarResId(),
                        user.isPinned(),
                        user.isMuted()
                );
                tempChatList.add(item);
            }

            Collections.sort(tempChatList, (c1, c2) -> Boolean.compare(c2.isPinned(), c1.isPinned()));

            requireActivity().runOnUiThread(() -> {
                chatList.clear();
                chatList.addAll(tempChatList);
                chatAdapter.notifyDataSetChanged();
            });
        });
    }

    private String formatTimestamp(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }

/*
    private void showNewChatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomDialog);
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_new_chat, null);
        EditText inputName = dialogView.findViewById(R.id.inputChatName);
        TextView btnCancel = dialogView.findViewById(R.id.btnCancels);
        TextView btnStart = dialogView.findViewById(R.id.btnStart);

        builder.setView(dialogView);
        builder.setTitle("Start New Chat");
        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnStart.setOnClickListener(view1 -> {
            String name = inputName.getText().toString().trim();
            if (!name.isEmpty()) {
                String chatId = "chat_" + System.currentTimeMillis();
                ExecutorService executor = Executors.newSingleThreadExecutor();

                executor.execute(() -> {
                    String currentUserId = SessionManager.getUserId(requireContext());
                    if (currentUserId == null) currentUserId = "guest";

                    AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext(), currentUserId);
                    ChatUserDao userDao = db.chatUserDao();
                    ChatUser newUser = new ChatUser(chatId, name, "", "", System.currentTimeMillis(), false, false, R.drawable.profilepic);
                    userDao.insertOrUpdate(newUser);

                    requireActivity().runOnUiThread(() -> {
                        dialog.dismiss();
                        Intent intent = new Intent(getContext(), ChatDetailActivity1.class);
                        intent.putExtra("chat_id", chatId);
                        intent.putExtra("chat_partner_name", name);
                        startActivity(intent);
                    });
                });
            }
        });

        dialog.show();
    }
*/
}
