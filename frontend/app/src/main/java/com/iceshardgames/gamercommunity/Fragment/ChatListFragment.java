package com.iceshardgames.gamercommunity.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
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

import com.iceshardgames.gamercommunity.APIintegration.ApiClient;
import com.iceshardgames.gamercommunity.APIintegration.ApiService;
import com.iceshardgames.gamercommunity.Activity.ChatScreen.ChatDetailActivity1;
import com.iceshardgames.gamercommunity.Adapter.ChannelAdapter;
import com.iceshardgames.gamercommunity.Adapter.ChatAdapter;
import com.iceshardgames.gamercommunity.Adapter.FriendAdapter;
import com.iceshardgames.gamercommunity.Adapter.FriendRequestAdapter;
import com.iceshardgames.gamercommunity.Adapter.UserSearchAdapter;
import com.iceshardgames.gamercommunity.DB.AppDatabase;
import com.iceshardgames.gamercommunity.DB.Channel;
import com.iceshardgames.gamercommunity.DB.ChatMessage;
import com.iceshardgames.gamercommunity.DB.ChatUser;
import com.iceshardgames.gamercommunity.DB.ChatUserDao;
import com.iceshardgames.gamercommunity.DB.FriendRequest;
import com.iceshardgames.gamercommunity.Model.BlockUserRequest;
import com.iceshardgames.gamercommunity.Model.ChatItem;
import com.iceshardgames.gamercommunity.Model.FriendListResponse;
import com.iceshardgames.gamercommunity.Model.FriendRequestResponse;
import com.iceshardgames.gamercommunity.Model.SendFriendRequest;
import com.iceshardgames.gamercommunity.Model.SendFriendRequestResponse;
import com.iceshardgames.gamercommunity.Model.UserSearchResponse;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.SessionManager;
import com.iceshardgames.gamercommunity.databinding.ActivityChatListBinding;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ChatListFragment extends Fragment {

    private ActivityChatListBinding binding;
    private List<ChatItem> chatList;
    private ChatAdapter chatAdapter;

    public ChatListFragment() {
        // Required empty public constructor
    }

    ItemTouchHelper.SimpleCallback swipeCallback = new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT) {

        @Override
        public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
            return false;
        }

        @Override
        public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            int position = viewHolder.getAdapterPosition();
            ChatItem chat = chatList.get(position);

            if (direction == ItemTouchHelper.LEFT) {
                chat.setMuted(!chat.isMuted());
                Toast.makeText(getActivity(), chat.isMuted() ? "Muted" : "Unmuted", Toast.LENGTH_SHORT).show();
            } else if (direction == ItemTouchHelper.RIGHT) {
                chat.setPinned(!chat.isPinned());
                Toast.makeText(getActivity(), chat.isPinned() ? "Pinned" : "Unpinned", Toast.LENGTH_SHORT).show();
            }

            Collections.sort(chatList, (c1, c2) -> Boolean.compare(c2.isPinned(), c1.isPinned()));
            chatAdapter.notifyDataSetChanged();
        }
    };

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = ActivityChatListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        binding.chatListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.searchResultRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.friendRequestRecycler.setLayoutManager(new LinearLayoutManager(getContext()));

        chatList = new ArrayList<>();
        chatAdapter = new ChatAdapter(getContext(), chatList);
        binding.chatListRecycler.setAdapter(chatAdapter);
        new ItemTouchHelper(swipeCallback).attachToRecyclerView(binding.chatListRecycler);

        binding.fabNewChat.setOnClickListener(v -> {
//            Intent intent = new Intent(getContext(), NewChatActivity.class);
//            startActivity(intent);
            showNewChatDialog();
//            showSearchUserDialog();
        });




        binding.searchChat.addTextChangedListener(new TextWatcher() {
            @Override

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
//                filterChats(s.toString());
                if (s.length() > 1) { // search after 2+ chars
                    searchUsersApi(s.toString());
                }else if (s.length() == 0) {
                    binding.searchResultRecycler.setVisibility(View.GONE);
                    binding.chatListRecycler.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                binding.ivClear.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);

            }
        });

        binding.ivClear.setOnClickListener(v -> {
            binding.searchChat.setText("");
            binding.searchResultRecycler.setVisibility(View.GONE);
            binding.chatListRecycler.setVisibility(View.VISIBLE);
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

        binding.exploreChannelsRecycler.setLayoutManager(new LinearLayoutManager(getContext(), LinearLayoutManager.HORIZONTAL, false));

        loadExploreChannels();
        loadPendding();

//        loadDummyChats();
        loadRecentChatUsers();
    }

    private void searchUsersApi(String query) {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);
        String token = "Bearer " + accessToken;
        Log.e("==pass", "searchUsersApi: " + accessToken);
        Log.e("==pass", "query: " + query);

        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        apiService.searchUsers(token, query, 1, 20, false).enqueue(new Callback<UserSearchResponse>() {
            @Override
            public void onResponse(Call<UserSearchResponse> call, Response<UserSearchResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<UserSearchResponse.User> users = response.body().getData().getUsers();

                    UserSearchAdapter adapter = new UserSearchAdapter(getContext(), users, user -> {
                        Toast.makeText(getContext(), "Clicked: " + user.getUsername(), Toast.LENGTH_SHORT).show();
                        // 👇 here you can open profile / send friend request
                        sendFriendRequest(user.getId());
                    });
                    binding.searchResultRecycler.setAdapter(adapter);
                    binding.chatListRecycler.setVisibility(View.GONE);
                    binding.searchResultRecycler.setVisibility(View.VISIBLE);
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
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);
        String token = "Bearer " + accessToken;

        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);

        Log.e("==pass", "userId: " + userId);

        apiService.sendFriendRequest(token, new SendFriendRequest(userId)).enqueue(new Callback<SendFriendRequestResponse>() {
            @Override
            public void onResponse(Call<SendFriendRequestResponse> call, Response<SendFriendRequestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    Toast.makeText(getContext(), "✅ " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("==api", "requestId = " + response.body().getData().getRequest().getId());
                    binding.friendRequestSection.setVisibility(View.GONE);
                    binding.exploreChannelsRecycler.setVisibility(View.VISIBLE);
                    binding.searchChatmain.setVisibility(View.VISIBLE);
                    binding.friendListRecycler.setVisibility(View.GONE);
                    binding.chatListRecycler.setVisibility(View.VISIBLE);
                    binding.searchResultRecycler.setVisibility(View.GONE);
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

    private void loadPendding() {
        // Insert dummy friend requests (only once)
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // ✅ Get current logged-in user
            String currentUserId = SessionManager.getUserId(requireContext());
            if (currentUserId == null) currentUserId = "guest"; // fallback

            // ✅ Use per-user database
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext(), currentUserId);


            if (db.friendRequestDao().getPendingRequests().isEmpty()) {
                db.friendRequestDao().insert(new FriendRequest(String.valueOf(System.currentTimeMillis()), null, "Alice", "pending"));
                db.friendRequestDao().insert(new FriendRequest(String.valueOf(System.currentTimeMillis() + 1), null, "Bob", "pending"));
                db.friendRequestDao().insert(new FriendRequest(String.valueOf(System.currentTimeMillis() + 2), null, "Charlie", "pending"));
            }
            // 👇 Refresh UI after inserting
            requireActivity().runOnUiThread(this::loadFriendRequests);
        });

    }

    private void loadExploreChannels() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // ✅ Get logged-in userId
            String currentUserId = SessionManager.getUserId(requireContext());
            if (currentUserId == null) currentUserId = "guest"; // fallback

            // ✅ Use per-user DB
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext(), currentUserId);


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
        if (tab.equals("Requests")) {
            // 👇 Show Friend Requests section
            binding.friendRequestSection.setVisibility(View.VISIBLE);
            binding.exploreChannelsRecycler.setVisibility(View.GONE);
            binding.searchChatmain.setVisibility(View.GONE);
            binding.chatListRecycler.setVisibility(View.GONE);
            binding.friendListRecycler.setVisibility(View.GONE);
            loadFriendRequests(); // new function
        }  else if (tab.equals("Friends")) {
            // 👉 Show Friends API data
            binding.friendRequestSection.setVisibility(View.GONE);
            binding.chatListRecycler.setVisibility(View.GONE);
            binding.exploreChannelsRecycler.setVisibility(View.GONE);
            binding.searchChatmain.setVisibility(View.GONE);
            binding.friendListRecycler.setVisibility(View.VISIBLE);
            loadFriendsList();
        }else {
            // 👇 Hide Friend Requests when on other tabs
            binding.friendRequestSection.setVisibility(View.GONE);
            binding.exploreChannelsRecycler.setVisibility(View.VISIBLE);
            binding.searchChatmain.setVisibility(View.VISIBLE);
            binding.friendListRecycler.setVisibility(View.GONE);
            binding.chatListRecycler.setVisibility(View.VISIBLE);
            loadRecentChatUsers(); // Refresh on return from ChatDetailActivity
            List<ChatItem> filtered = new ArrayList<>();
/*
            chatAdapter = new ChatAdapter(getContext(), filtered);
            binding.chatListRecycler.setAdapter(chatAdapter);*/
            requireActivity().runOnUiThread(() -> updateChatList(filtered));

            /*
            for (ChatItem item : chatList) {
                switch (tab) {
                    case "Messages":
                        filtered.add(item); // All shown
                        break;
                    case "Unread":
                        if (!item.isMuted()) // Dummy logic: not muted = unread
                            filtered.add(item);
                        break;
                }
            }
*/
        }
    }

    //dummy frds
/*
    private void loadFriendRequests() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            AppDatabase db = AppDatabase.getInstance(requireContext());
            List<FriendRequest> pendingRequests = db.friendRequestDao().getPendingRequests();

            requireActivity().runOnUiThread(() -> {
                FriendRequestAdapter adapter = new FriendRequestAdapter(
                        getContext(),
                        pendingRequests,
                        request -> Executors.newSingleThreadExecutor().execute(() -> {
                            // ✅ Accept request
                            db.friendRequestDao().updateRequestStatus(request.getId(), "accepted");

                            // ✅ Create new ChatUser when accepted
                            String chatId = "chat_" + System.currentTimeMillis();
                            ChatUser newUser = new ChatUser(
                                    chatId,
                                    request.getSenderName(),
                                    "",
                                    "",
                                    System.currentTimeMillis(),
                                    false,
                                    false,
                                    R.drawable.profilepic
                            );
                            db.chatUserDao().insertOrUpdate(newUser);

                            // Refresh requests
                            requireActivity().runOnUiThread(this::loadFriendRequests);

                            // Refresh chat list
                            requireActivity().runOnUiThread(this::loadRecentChatUsers);
                        }),
                        request -> Executors.newSingleThreadExecutor().execute(() -> {
                            // ❌ Decline
                            db.friendRequestDao().updateRequestStatus(request.getId(), "declined");
                            requireActivity().runOnUiThread(this::loadFriendRequests);
                        }),
                        request -> Executors.newSingleThreadExecutor().execute(() -> {
                            // 🚫 Block
                            db.friendRequestDao().updateRequestStatus(request.getId(), "blocked");
                            requireActivity().runOnUiThread(this::loadFriendRequests);
                        })
                );
                binding.friendRequestRecycler.setAdapter(adapter);
            });
        });
    }
*/

    private void updateChatList(List<ChatItem> newChats) {
        chatList.clear();
        chatList.addAll(newChats);
        if (chatAdapter != null) {
            chatAdapter.notifyDataSetChanged();
        }
    }


    private void loadFriendRequests() {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);
        String token = "Bearer " + accessToken; // 👈 use your token
        Log.e("==pass", "loadFriendRequests: " + token);
        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        apiService.getFriendRequests(token, "incoming", 1, 20).enqueue(new retrofit2.Callback<FriendRequestResponse>() {
            @Override
            public void onResponse(Call<FriendRequestResponse> call, retrofit2.Response<FriendRequestResponse> response) {
                Log.e("==pass", "onResponse: ");
                if (response.isSuccessful() && response.body() != null) {

                    List<FriendRequestResponse.Request> requests = response.body().getData().getRequests();

                    // ✅ Build local list with requestId
                    List<com.iceshardgames.gamercommunity.DB.FriendRequest> localList = new ArrayList<>();
                    for (FriendRequestResponse.Request req : requests) {
                        localList.add(new com.iceshardgames.gamercommunity.DB.FriendRequest(req.getId(),                      // requestId
                                req.getUser().getId(),            // userId
                                req.getUser().getUsername(), "pending"));
                    }

                    FriendRequestAdapter adapter = new FriendRequestAdapter(getContext(), localList, request -> {
                        Toast.makeText(getContext(), "Accepted " + request.getSenderName(), Toast.LENGTH_SHORT).show();
                        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        String accessToken = prefs.getString("accessToken", null);

                        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
                        Log.e("==pass", "accessToken " + "Bearer " + accessToken);
                        Log.e("==pass", "request.getRequestId() " + request.getRequestId());

                        apiService.acceptFriendRequest("Bearer " + accessToken, request.getRequestId()).enqueue(new retrofit2.Callback<FriendRequestResponse>() {
                            @Override
                            public void onResponse(Call<FriendRequestResponse> call, retrofit2.Response<FriendRequestResponse> response) {
                                if (response.isSuccessful() && response.body() != null) {
                                    Toast.makeText(getContext(), "✅ " + response.body().getMessage(), Toast.LENGTH_SHORT).show();
                                    Log.e("==pass", "success");

                                    requireActivity().runOnUiThread(() -> {
                                        loadFriendRequests(); // only refresh the friend list
                                    });

                                    /*// ✅ Add new ChatUser locally
                                    String name = request.getSenderName();
                                    String chatId = "chat_" + System.currentTimeMillis(); // unique ID

                                    String currentUserId = SessionManager.getUserId(requireContext());
                                    if (currentUserId == null) currentUserId = "guest";

                                    AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext(), currentUserId);
                                    ExecutorService executor = Executors.newSingleThreadExecutor();
                                    executor.execute(() -> {
                                        ChatUserDao userDao = db.chatUserDao();

                                        ChatUser newUser = new ChatUser(
                                                chatId,
                                                name,
                                                "", // you can also store userId here if you want
                                                "", // avatar url if available
                                                System.currentTimeMillis(),
                                                false,
                                                false,
                                                R.drawable.profilepic
                                        );

                                        userDao.insertOrUpdate(newUser);

                                        requireActivity().runOnUiThread(() -> {
                                            // Refresh lists
                                            loadFriendRequests();
                                            loadRecentChatUsers();
                                        });
                                    });*/
                                    /*String name = request.getSenderName();
                                    if (!name.isEmpty()) {
                                        String chatId = "chat_" + System.currentTimeMillis(); // unique ID
                                        AppDatabase db = AppDatabase.getInstance(requireContext());
                                        ExecutorService executor = Executors.newSingleThreadExecutor();

                                        executor.execute(() -> {
                                            ChatUserDao userDao = db.chatUserDao();

                                            ChatUser newUser = new ChatUser(chatId, name, "", "", System.currentTimeMillis(), false, false, R.drawable.profilepic);

                                            userDao.insertOrUpdate(newUser);

                                            requireActivity().runOnUiThread(() -> {
                                                Intent intent = new Intent(getContext(), ChatDetailActivity.class);
                                                intent.putExtra("chat_id", chatId);
                                                intent.putExtra("chat_partner_name", name); // 👈 Add this
                                                Log.e("==sana", "fragment: " + name);
                                                startActivity(intent);
                                            });
                                        });
                                    }*/
                                } else {
                                    try {
                                        String err = response.errorBody() != null ? response.errorBody().string() : "Unknown";
                                        Toast.makeText(getContext(), "❌ Failed: " + err, Toast.LENGTH_LONG).show();
                                        Log.e("==error", err);
                                    } catch (Exception e) {
                                        e.printStackTrace();
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<FriendRequestResponse> call, Throwable t) {
                                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }, request -> {
                        Toast.makeText(getContext(), "Declined " + request.getSenderName(), Toast.LENGTH_SHORT).show();
                        // 👇 Block clicked
                        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        String accessToken = prefs.getString("accessToken", null);
                        String token = "Bearer " + accessToken;

                        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);

                        Log.e("==debug", "Declining requestId = " + request.getRequestId());

                        apiService.declineFriendRequest(token, request.getRequestId()).enqueue(new Callback<FriendRequestResponse>() {
                            @Override
                            public void onResponse(Call<FriendRequestResponse> call, Response<FriendRequestResponse> resp) {
                                if (resp.isSuccessful() && resp.body() != null) {
                                    Toast.makeText(getContext(), "❌ " + resp.body().getMessage(), Toast.LENGTH_SHORT).show();
                                    // Refresh after decline
                                    loadFriendRequests();
                                    loadRecentChatUsers();
                                } else {
                                    try {
                                        String err = resp.errorBody() != null ? resp.errorBody().string() : "Unknown error";
                                        Log.e("==error", err);
                                        Toast.makeText(getContext(), "❌ " + err, Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        Log.e("==error", "Parse error", e);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<FriendRequestResponse> call, Throwable t) {
                                Log.e("==fail", "Decline failed: " + t.getMessage(), t);
                                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    }, request -> {
                        Toast.makeText(getContext(), "Blocking " + request.getSenderName(), Toast.LENGTH_SHORT).show();

                        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
                        String accessToken = prefs.getString("accessToken", null);
                        String token = "Bearer " + accessToken;

                        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);

                        // 👇 We need the userId (not the request id here)
                        String userId = request.getUserId();

                        apiService.blockUser(token, new BlockUserRequest(userId)).enqueue(new Callback<FriendRequestResponse>() {
                            @Override
                            public void onResponse(Call<FriendRequestResponse> call, Response<FriendRequestResponse> resp) {
                                if (resp.isSuccessful() && resp.body() != null) {
                                    Toast.makeText(getContext(), "🚫 " + resp.body().getMessage(), Toast.LENGTH_SHORT).show();
                                    loadFriendRequests();
                                    loadRecentChatUsers();
                                } else {
                                    try {
                                        String err = resp.errorBody() != null ? resp.errorBody().string() : "Unknown error";
                                        Log.e("==error", err);
                                        Toast.makeText(getContext(), "❌ " + err, Toast.LENGTH_LONG).show();
                                    } catch (Exception e) {
                                        Log.e("==error", "Parse error", e);
                                    }
                                }
                            }

                            @Override
                            public void onFailure(Call<FriendRequestResponse> call, Throwable t) {
                                Log.e("==fail", "Block failed: " + t.getMessage(), t);
                                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                            }
                        });
                    });
                    binding.friendRequestRecycler.setAdapter(adapter);
                } else {
                    if (response.errorBody() != null) {
                        try {
                            String errorMsg = response.errorBody().string();
                            Toast.makeText(getContext(), "Error: " + errorMsg, Toast.LENGTH_LONG).show();
                            Log.e("==pass", "API Error: " + errorMsg);
                        } catch (Exception e) {
                            Toast.makeText(getContext(), "Unknown error", Toast.LENGTH_SHORT).show();
                            Log.e("==pass", "Error parsing error body", e);
                        }
                    } else {
                        Toast.makeText(getContext(), "No data found", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<FriendRequestResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
    private void loadFriendsList() {
        SharedPreferences prefs = getActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);
        String token = "Bearer " + accessToken;

        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        apiService.getFriends(token, 1, 20).enqueue(new Callback<FriendListResponse>() {
            @Override
            public void onResponse(Call<FriendListResponse> call, Response<FriendListResponse> response) {
                Log.e("==pass", "onResponse: "+response.isSuccessful() );
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<FriendListResponse.Friend> friends = response.body().getData().getFriends();
                    FriendAdapter friendAdapter = new FriendAdapter(getContext(), friends);
                    binding.friendListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
                    binding.friendListRecycler.setHasFixedSize(true);
                    binding.friendListRecycler.setAdapter(friendAdapter);
                    binding.friendListRecycler.setVisibility(View.VISIBLE);
                    Log.d("==pass", "Loaded friends: " + friends.size());

                } else {
                    Toast.makeText(getContext(), "Failed to load friends", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FriendListResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void loadRecentChatUsers() {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            // ✅ Get current logged-in userId
            String currentUserId = SessionManager.getUserId(requireContext());
            if (currentUserId == null) currentUserId = "guest";

            // ✅ Use per-user DB
            AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext(), currentUserId);
            List<ChatUser> chatUsers = db.chatUserDao().getAllUsers();
            List<ChatItem> tempChatList = new ArrayList<>();

            for (ChatUser user : chatUsers) {
                ChatMessage lastMsg = db.chatMessageDao().getLastMessageForChat(user.getChatId());

                String lastMessageText = lastMsg != null ? lastMsg.getMessage() : "";
                String lastMessageTime = lastMsg != null ? formatTimestamp(lastMsg.getTimestamp()) : "";

                ChatItem item = new ChatItem(user.getChatId(), user.getUserName(), lastMessageText, lastMessageTime, user.getAvatarResId(), user.isPinned(), user.isMuted());

                tempChatList.add(item);
            }

            Collections.sort(tempChatList, (c1, c2) -> {
                if (c1.isPinned() && !c2.isPinned()) return -1;
                if (!c1.isPinned() && c2.isPinned()) return 1;
                return 0;
            });

            requireActivity().runOnUiThread(() -> updateChatList(tempChatList));

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

        chatList.add(new ChatItem("1", "Alex", "Let’s play tonight!", "4:21 PM", R.drawable.profilepic, false, false));
        chatList.add(new ChatItem("1", "Blaze", "New game update", "3:05 PM", R.drawable.profilepic, true, false));
        chatList.add(new ChatItem("1", "Nova", "Check the forum", "1:17 PM", R.drawable.profilepic, false, true));

        chatAdapter.notifyDataSetChanged();
    }

    @Override
    public void onResume() {
        super.onResume();
        binding.chatListRecycler.setVisibility(View.VISIBLE);
        binding.searchResultRecycler.setVisibility(View.GONE);
        binding.friendListRecycler.setVisibility(View.GONE);
        loadRecentChatUsers();
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


    private void showSearchUserDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.CustomDialog);
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_search_user, null);
        EditText inputName = dialogView.findViewById(R.id.inputUserName);
        TextView btnCancel = dialogView.findViewById(R.id.btnCancelSearch);
        TextView btnSend = dialogView.findViewById(R.id.btnSendRequest);

        builder.setView(dialogView);
        builder.setTitle("Search User");

        AlertDialog dialog = builder.create();

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSend.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();

            ExecutorService executor = Executors.newSingleThreadExecutor();
            executor.execute(() -> {
                // ✅ Get current userId
                String currentUserId = SessionManager.getUserId(requireContext());
                if (currentUserId == null) currentUserId = "guest";

                // ✅ Use per-user DB
                AppDatabase db = AppDatabase.getInstance(requireContext().getApplicationContext(), currentUserId);


                // ✅ Check if already exists
                FriendRequest existing = db.friendRequestDao().findPendingRequest(name);
                if (existing == null) {
                    db.friendRequestDao().insert(new FriendRequest(String.valueOf(System.currentTimeMillis()), null, name, "pending"));

                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Friend request sent to " + name, Toast.LENGTH_SHORT).show();
                        dialog.dismiss();
                        loadFriendRequests(); // Refresh requests tab
                    });
                } else {
                    requireActivity().runOnUiThread(() -> {
                        Toast.makeText(getContext(), "Request already sent to " + name, Toast.LENGTH_SHORT).show();
                    });
                }
            });
        });

        dialog.show();
    }

}
