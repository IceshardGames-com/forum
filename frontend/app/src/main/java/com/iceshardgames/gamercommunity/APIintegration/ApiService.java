package com.iceshardgames.gamercommunity.APIintegration;

import com.iceshardgames.gamercommunity.Activity.LoginScreen.LoginRequest;
import com.iceshardgames.gamercommunity.Activity.LoginScreen.LoginResponse;
import com.iceshardgames.gamercommunity.Activity.OtpScreen.ChangePasswordRequest;
import com.iceshardgames.gamercommunity.Activity.OtpScreen.ChangePasswordResponse;
import com.iceshardgames.gamercommunity.Activity.OtpScreen.OtpRequest;
import com.iceshardgames.gamercommunity.Activity.OtpScreen.OtpResponse;
import com.iceshardgames.gamercommunity.Activity.OtpScreen.OtpVerifyResponse;
import com.iceshardgames.gamercommunity.Activity.OtpScreen.VerifyOtpRequest;
import com.iceshardgames.gamercommunity.Activity.ProfileScreen.InterestsResponse;
import com.iceshardgames.gamercommunity.Activity.RegisterScreen.RegisterRequest;
import com.iceshardgames.gamercommunity.Activity.RegisterScreen.RegisterResponse;
import com.iceshardgames.gamercommunity.Model.BlockUserRequest;
import com.iceshardgames.gamercommunity.Model.ConversationCreateRequest;
import com.iceshardgames.gamercommunity.Model.ConversationResponse;
import com.iceshardgames.gamercommunity.Model.DeviceRegistrationRequest;
import com.iceshardgames.gamercommunity.Model.DevicesResponse;
import com.iceshardgames.gamercommunity.Model.FriendListResponse;
import com.iceshardgames.gamercommunity.Model.FriendRequestResponse;
import com.iceshardgames.gamercommunity.Model.MessagesResponse;
import com.iceshardgames.gamercommunity.Model.SendFriendRequest;
import com.iceshardgames.gamercommunity.Model.SendFriendRequestResponse;
import com.iceshardgames.gamercommunity.Model.SendMessageRequest;
import com.iceshardgames.gamercommunity.Model.UserSearchResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @Headers("Content-Type: application/json")
    @POST("/api/auth/login")
    Call<LoginResponse> loginUser(@Body LoginRequest loginRequest);

    @Headers("Content-Type: application/json")
    @POST("/api/auth/register")
    Call<RegisterResponse> registerUser(@Body RegisterRequest request);

    @POST("/api/auth/otp/request")
    Call<OtpResponse> requestOtp(@Body OtpRequest otpRequest);

    @POST("/api/auth/otp/verify")
    Call<OtpVerifyResponse> verifyOtp(@Body VerifyOtpRequest request);

    @Headers("Content-Type: application/json")

    @PUT("/api/auth/change-password")
    Call<ChangePasswordResponse> changePassword(
            @Header("Authorization") String bearerToken,
            @Body ChangePasswordRequest request
    );

    /*@GET("/api/interests")
    Call<InterestsResponse> getInterests();*/

    @GET("/api/interests")
    Call<InterestsResponse> getInterests(
            @Query("page") int page,
            @Query("limit") int limit
    );

    @GET("/api/friends/requests")
    Call<FriendRequestResponse> getFriendRequests(
            @Header("Authorization") String bearerToken,
            @Query("type") String type,   // incoming / outgoing
            @Query("page") int page,
            @Query("limit") int limit
    );

    @PATCH("/api/friends/accept/{id}")
    Call<FriendRequestResponse> acceptFriendRequest(
            @Header("Authorization") String token,
            @Path("id") String requestId
    );
    @PATCH("/api/friends/decline/{id}")
    Call<FriendRequestResponse> declineFriendRequest(
            @Header("Authorization") String token,
            @Path("id") String requestId
    );

    @POST("/api/friends/block")
    Call<FriendRequestResponse> blockUser(
            @Header("Authorization") String token,
            @Body BlockUserRequest body
    );

    @POST("/api/friends/request")
    Call<SendFriendRequestResponse> sendFriendRequest(
            @Header("Authorization") String token,
            @Body SendFriendRequest request
    );
    @GET("/api/users/search")
    Call<UserSearchResponse> searchUsers(
            @Header("Authorization") String token,
            @Query("q") String query,
            @Query("page") int page,
            @Query("limit") int limit,
            @Query("includeInactive") boolean includeInactive
    );
    @GET("/api/friends")
    Call<FriendListResponse> getFriends(
            @Header("Authorization") String token,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // ApiService.java  (append to your existing interface)
    @POST("/api/devices/register")
    Call<DevicesResponse> registerDevice(
            @Header("Authorization") String token,
            @Body DeviceRegistrationRequest body
    );

    @GET("/api/devices/user/{userId}")
    Call<DevicesResponse> getDevicesForUser(
            @Header("Authorization") String token,
            @Path("userId") String userId
    );

    // Conversations
    @POST("/api/conversations")
    Call<ConversationResponse> createOrGetConversation(
            @Header("Authorization") String token,
            @Body ConversationCreateRequest body
    );

    @GET("/api/conversations/{conversationId}")
    Call<ConversationResponse> getConversation(
            @Header("Authorization") String token,
            @Path("conversationId") String conversationId
    );

    // Messages
    @POST("/api/messages/conversations/{conversationId}")
    Call<MessagesResponse> sendMessage(
            @Header("Authorization") String token,
            @Path("conversationId") String conversationId,
            @Body SendMessageRequest body
    );

    @GET("/api/messages/conversations/{conversationId}")
    Call<MessagesResponse> getMessages(
            @Header("Authorization") String token,
            @Path("conversationId") String conversationId,
            @Query("page") int page,
            @Query("limit") int limit
    );

    // Delivery / Read
    @PATCH("/api/messages/{messageId}/delivered")
    Call<Void> markDelivered(
            @Header("Authorization") String token,
            @Path("messageId") String messageId
    );

    @PATCH("/api/messages/{messageId}/read")
    Call<Void> markRead(
            @Header("Authorization") String token,
            @Path("messageId") String messageId
    );


}
