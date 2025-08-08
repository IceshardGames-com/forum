package com.iceshardgames.gamercommunity.Fragment;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.tabs.TabLayout;
import com.iceshardgames.gamercommunity.Activity.ChatScreen.ChatDetailActivity;
import com.iceshardgames.gamercommunity.Activity.ChatScreen.NewChatActivity;
import com.iceshardgames.gamercommunity.Adapter.ChannelAdapter;
import com.iceshardgames.gamercommunity.Adapter.ChatAdapter;
import com.iceshardgames.gamercommunity.DB.AppDatabase;
import com.iceshardgames.gamercommunity.DB.Channel;
import com.iceshardgames.gamercommunity.DB.ChatMessage;
import com.iceshardgames.gamercommunity.DB.ChatUser;
import com.iceshardgames.gamercommunity.DB.ChatUserDao;
import com.iceshardgames.gamercommunity.Model.ChatItem;
import com.iceshardgames.gamercommunity.Model.PostModel;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.databinding.ActivityChatListBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ChatListFragment extends Fragment {

    private ActivityChatListBinding binding;
    private List<ChatItem> chatList;
    private ChatAdapter chatAdapter;

    public ChatListFragment() {
        // Required empty public constructor
    }
    ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0,
            ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder,
                              @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            ChatItem chat = chatList.get(position);

            if (direction == ItemTouchHelper.LEFT) {
                chat.setMuted(!chat.isMuted());
                Toast.makeText(getActivity(),
                        chat.isMuted() ? "Muted" : "Unmuted", Toast.LENGTH_SHORT).show();
            } else if (direction == ItemTouchHelper.RIGHT) {
                chat.setPinned(!chat.isPinned());
                Toast.makeText(getActivity(),
                        chat.isPinned() ? "Pinned" : "Unpinned", Toast.LENGTH_SHORT).show();
            }

            Collections.sort(chatList, (c1, c2) -> Boolean.compare(c2.isPinned(), c1.isPinned()));
            chatAdapter.notifyDataSetChanged();
        }
    };
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = ActivityChatListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.chatListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(getContext(), chatList);
        binding.chatListRecycler.setAdapter(chatAdapter);
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.chatListRecycler);

        binding.fabNewChat.setOnClickListener(v -> {
//            Intent intent = new Intent(getContext(), NewChatActivity.class);
//            startActivity(intent);
            showNewChatDialog();
        });

        binding.searchChat.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterChats(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Tab Clicks
        binding.tabMessages.setSelected(true); // Default tab
        View.OnClickListener tabClickListener = v -> {
            binding.tabMessages.setSelected(false);
            binding.tabThreads.setSelected(false);
            binding.tabUnread.setSelected(false);
            binding.tabRequests.setSelected(false);
            v.setSelected(true);

            String selectedTab = ((TextView) v).getText().toString();
            filterByTab(selectedTab);
        };

        binding.tabMessages.setOnClickListener(tabClickListener);
        binding.tabThreads.setOnClickListener(tabClickListener);
        binding.tabUnread.setOnClickListener(tabClickListener);
        binding.tabRequests.setOnClickListener(tabClickListener);

        binding.exploreChannelsRecycler.setLayoutManager(
                new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false)
        );
        loadExploreChannels();

//        loadDummyChats();
        loadRecentChatUsers();
    }

    private void loadExploreChannels() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());

            // Dummy insert once
            if (db.channelDao().getAllChannels().isEmpty()) {
                db.channelDao().insert(new Channel("1", "FPS Gaming", 234, R.drawable.profilepic, false));
                db.channelDao().insert(new Channel("2", "MMORPG Fans", 120, R.drawable.profilepic, true));
                db.channelDao().insert(new Channel("3", "Indie Games", 85, R.drawable.profilepic, false));
            }

            List<Channel> channelList = db.channelDao().getAllChannels();

            requireActivity().runOnUiThread(() -> {
                ChannelAdapter adapter = new ChannelAdapter(getContext(), channelList);
                binding.exploreChannelsRecycler.setAdapter(adapter);
            });
        });
    }


    private void filterByTab(String tab) {
        List<ChatItem> filtered = new ArrayList<>();

        for (ChatItem item : chatList) {
            switch (tab) {
                case "Messages":
                    filtered.add(item); // All shown
                    break;
                case "Threads":
                    if (item.getName().contains("Thread")) // Replace with real logic
                        filtered.add(item);
                    break;
                case "Unread":
                    if (!item.isMuted()) // Dummy logic: not muted = unread
                        filtered.add(item);
                    break;
                case "Requests":
                    if (item.getName().contains("Request")) // Dummy tag logic
                        filtered.add(item);
                    break;
            }
        }

        chatAdapter = new ChatAdapter(getContext(), filtered);
        binding.chatListRecycler.setAdapter(chatAdapter);
    }

    private void loadRecentChatUsers() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<ChatUser> chatUsers = db.chatUserDao().getAllUsers();
            List<ChatItem> tempChatList = new ArrayList<>();

            for (ChatUser user : chatUsers) {
                ChatMessage lastMsg = db.chatMessageDao().getLastMessageForChat(user.getChatId());

                String lastMessageText = lastMsg != null ? lastMsg.getMessage() : "";
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

            Collections.sort(tempChatList, (c1, c2) -> {
                if (c1.isPinned() && !c2.isPinned()) return -1;
                if (!c1.isPinned() && c2.isPinned()) return 1;
                return 0;
            });

            requireActivity().runOnUiThread(() -> {
                chatList.clear();
                chatList.addAll(tempChatList);
                if (chatAdapter != null) {
                    chatAdapter.notifyDataSetChanged();
                }
            });
        });

    }
    private String formatTimestamp(long timestamp) {
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("hh:mm a", java.util.Locale.getDefault());
        return sdf.format(new java.util.Date(timestamp));
    }

    private void filterChats(String query) {
        List<ChatItem> filtered = new ArrayList<>();
        for (ChatItem chat : chatList) {
            if (chat.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(chat);
            }
        }

        Collections.sort(chatList, (c1, c2) -> Boolean.compare(c2.isPinned(), c1.isPinned()));
        chatAdapter = new ChatAdapter(getContext(), filtered);
        binding.chatListRecycler.setAdapter(chatAdapter);
    }

    private void loadDummyChats() {
        chatList.clear();

        chatList.add(new ChatItem("1","Alex", "Let’s play tonight!", "4:21 PM", R.drawable.profilepic, false, false));
        chatList.add(new ChatItem("1","Blaze", "New game update", "3:05 PM", R.drawable.profilepic, true, false));
        chatList.add(new ChatItem("1","Nova", "Check the forum", "1:17 PM", R.drawable.profilepic, false, true));

        chatAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        loadRecentChatUsers(); // Refresh on return from ChatDetailActivity
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    private void showNewChatDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomDialog);
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_new_chat, null);
        EditText inputName = dialogView.findViewById(R.id.inputChatName);
        TextView btnCancel = dialogView.findViewById(R.id.btnCancels);
        TextView btnStart = dialogView.findViewById(R.id.btnStart);

        builder.setView(dialogView);
        builder.setTitle("Start New Chat");

        // Create dialog and keep reference
        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnStart.setOnClickListener(view1 -> {
            String name = inputName.getText().toString().trim();
            if (!name.isEmpty()) {
                String chatId = "chat_" + System.currentTimeMillis(); // unique ID
                AppDatabase db = AppDatabase.getInstance(requireContext());
                ExecutorService executor = Executors.newSingleThreadExecutor();

                executor.execute(() -> {
                    ChatUserDao userDao = db.chatUserDao();

                    ChatUser newUser = new ChatUser(
                            chatId,
                            name,
                            "",
                            "",
                            System.currentTimeMillis(),
                            false,
                            false,
                            R.drawable.profilepic
                    );

                    userDao.insertOrUpdate(newUser);

                    requireActivity().runOnUiThread(() -> {
                        dialog.dismiss(); // Dismiss the same dialog
                        Intent intent = new Intent(getContext(), ChatDetailActivity.class);
                        intent.putExtra("chat_id", chatId);
                        intent.putExtra("chat_partner_name", name); // 👈 Add this
                        Log.e("==sana", "fragment: "+name );
                        startActivity(intent);
                    });
                });
            }
        });

        dialog.show();
    }



}
