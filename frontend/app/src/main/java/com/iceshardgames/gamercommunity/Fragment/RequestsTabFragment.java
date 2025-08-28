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
import com.iceshardgames.gamercommunity.Adapter.FriendRequestAdapter;
import com.iceshardgames.gamercommunity.DB.FriendRequest;
import com.iceshardgames.gamercommunity.Model.Request.BlockUserRequest;
import com.iceshardgames.gamercommunity.Model.Response.FriendRequestResponse;
import com.iceshardgames.gamercommunity.R;
import java.util.ArrayList;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RequestsTabFragment extends Fragment {
    private RecyclerView friendRequestRecycler;
    private View tvNoRequests;

    @Nullable @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_tab_requests, container, false);
    }

    @Override public void onViewCreated(@NonNull View v, @Nullable Bundle s) {
        friendRequestRecycler = v.findViewById(R.id.friendRequestRecycler);
        tvNoRequests = v.findViewById(R.id.tv_no_requests);
        friendRequestRecycler.setLayoutManager(new LinearLayoutManager(getContext()));
        loadFriendRequests();
    }

    private void loadFriendRequests() {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);
        String token = "Bearer " + accessToken;

        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        apiService.getFriendRequests(token, "incoming", 1, 20).enqueue(new Callback<FriendRequestResponse>() {
            @Override
            public void onResponse(Call<FriendRequestResponse> call, Response<FriendRequestResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<FriendRequestResponse.Request> requests = response.body().getData().getRequests();
                    List<FriendRequest> localList = new ArrayList<>();
                    if ((requests == null || requests.isEmpty())) {
                        tvNoRequests.setVisibility(View.VISIBLE);
                        friendRequestRecycler.setVisibility(View.GONE);
                    } else {
                        tvNoRequests.setVisibility(View.GONE);
                        friendRequestRecycler.setVisibility(View.VISIBLE);
                    }
                    for (FriendRequestResponse.Request req : requests) {
                        localList.add(new FriendRequest(
                                req.getId(), // requestId
                                req.getUser().getId(),
                                req.getUser().getUsername(),
                                "pending"
                        ));
                    }

                    FriendRequestAdapter adapter = new FriendRequestAdapter(
                            getContext(),
                            localList,
                            request -> acceptRequest(request.getRequestId()),
                            request -> declineRequest(request.getRequestId()),
                            request -> blockUser(request.getUserId())
                    );
                    friendRequestRecycler.setAdapter(adapter);


                } else {
                    tvNoRequests.setVisibility(View.VISIBLE);
                    friendRequestRecycler.setVisibility(View.GONE);
                }
            }

            @Override
            public void onFailure(Call<FriendRequestResponse> call, Throwable t) {
                tvNoRequests.setVisibility(View.VISIBLE);
                friendRequestRecycler.setVisibility(View.GONE);
                Toast.makeText(getContext(), "Error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void acceptRequest(String requestId) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);
        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        apiService.acceptFriendRequest("Bearer " + accessToken, requestId).enqueue(new Callback<FriendRequestResponse>() {
            @Override public void onResponse(Call<FriendRequestResponse> call, Response<FriendRequestResponse> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "Request accepted", Toast.LENGTH_SHORT).show();
                    loadFriendRequests();
                } else {
                    Toast.makeText(getContext(), "Failed to accept", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<FriendRequestResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void declineRequest(String requestId) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);
        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        apiService.declineFriendRequest("Bearer " + accessToken, requestId).enqueue(new Callback<FriendRequestResponse>() {
            @Override public void onResponse(Call<FriendRequestResponse> call, Response<FriendRequestResponse> resp) {
                if (resp.isSuccessful()) {
                    Toast.makeText(getContext(), "Declined", Toast.LENGTH_SHORT).show();
                    loadFriendRequests();
                } else {
                    Toast.makeText(getContext(), "Failed to decline", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<FriendRequestResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void blockUser(String userId) {
        SharedPreferences prefs = requireActivity().getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String accessToken = prefs.getString("accessToken", null);
        ApiService apiService = ApiClient.getRetrofit().create(ApiService.class);
        apiService.blockUser("Bearer " + accessToken, new BlockUserRequest(userId)).enqueue(new Callback<FriendRequestResponse>() {
            @Override public void onResponse(Call<FriendRequestResponse> call, Response<FriendRequestResponse> resp) {
                if (resp.isSuccessful()) {
                    Toast.makeText(getContext(), "Blocked", Toast.LENGTH_SHORT).show();
                    loadFriendRequests();
                } else {
                    Toast.makeText(getContext(), "Failed to block", Toast.LENGTH_SHORT).show();
                }
            }
            @Override public void onFailure(Call<FriendRequestResponse> call, Throwable t) {
                Toast.makeText(getContext(), "Network error: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
