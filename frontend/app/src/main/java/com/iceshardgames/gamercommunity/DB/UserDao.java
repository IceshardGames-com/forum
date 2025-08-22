package com.iceshardgames.gamercommunity.DB;


import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;

@Dao
public interface UserDao {

    @Insert
    void insert(User user);

    @Query("SELECT * FROM chat_users WHERE userName LIKE :name")
    List<ChatUser> searchUsers(String name);


    @Query("SELECT * FROM users")
    List<User> getAllUsers();
}

