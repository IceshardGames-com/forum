// DB/FriendRequestDao.java
package com.iceshardgames.gamercommunity.DB;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface FriendRequestDao {

    @Insert
    void insert(FriendRequest request);

    @Update
    void update(FriendRequest request);

    @Query("UPDATE friend_requests SET status = :status WHERE id = :id")
    void updateRequestStatus(int id, String status);

    @Query("SELECT * FROM friend_requests WHERE status = 'pending'")
    List<FriendRequest> getPendingRequests();
    // ✅ Check if request already exists
    @Query("SELECT * FROM friend_requests WHERE senderName = :name AND status = 'pending' LIMIT 1")
    FriendRequest findPendingRequest(String name);
}
