// DB/ChannelDao.java
package com.iceshardgames.gamercommunity.DB;

import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ChannelDao {
    @Query("SELECT * FROM channels")
    List<Channel> getAllChannels();

    @Insert
    void insert(Channel channel);

    @Update
    void update(Channel channel);
}
