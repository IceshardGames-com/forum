package com.iceshardgames.gamercommunity.Fragment;

import static android.content.Context.MODE_PRIVATE;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.APIintegration.ApiClient;
import com.iceshardgames.gamercommunity.APIintegration.ApiService;
import com.iceshardgames.gamercommunity.Adapter.FriendAdapter;
import com.iceshardgames.gamercommunity.Model.Response.FriendListResponse;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.Utills.Utills;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class FriendsTabFragment extends Fragment {
    private RecyclerView friendListRecycler;
    private View tvNoFriends;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_friends, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        friendListRecycler = v.findViewById(R.id.friendListRecycler);
        tvNoFriends = v.findViewById(R.id.tv_no_friends);
        friendListRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        loadFriendsList();
    }

    private void loadFriendsList() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);
        String token = "Bearer " + accessToken;

        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        apiService.getFriends(token, 1, 20).enqueue(new Callback<FriendListResponse>() {
            @Override
            public void onResponse(Call<FriendListResponse> call, Response<FriendListResponse> response) {
                if (response.isSuccessful() && response.body() != null && response.body().isSuccess()) {
                    List<FriendListResponse.Friend> friends = response.body().getData().getFriends();
                    if (friends == null || friends.isEmpty()) {
                        friendListRecycler.setVisibility(View.GONE);
                        tvNoFriends.setVisibility(View.VISIBLE);
                    } else {
                        FriendAdapter friendAdapter = new FriendAdapter(getContext(), friends);
                        friendListRecycler.setAdapter(friendAdapter);
                        friendListRecycler.setVisibility(View.VISIBLE);
                        tvNoFriends.setVisibility(View.GONE);
                    }
                } else {
                    friendListRecycler.setVisibility(View.GONE);
                    tvNoFriends.setVisibility(View.VISIBLE);
                    Toast.makeText(getContext(), "Failed to load friends", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<FriendListResponse> call, Throwable t) {
                if (isAdded()) {
                    requireActivity().runOnUiThread(() -> {
                        friendListRecycler.setVisibility(View.GONE);
                        tvNoFriends.setVisibility(View.VISIBLE);
                        Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    });
                }
            }
        });
    }
}
