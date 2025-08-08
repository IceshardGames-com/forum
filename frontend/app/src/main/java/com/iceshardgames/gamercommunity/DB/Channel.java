// DB/Channel.java
package com.iceshardgames.gamercommunity.DB;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

@Entity(tableName = "channels")
public class Channel {
    @PrimaryKey
    @NonNull
    public String id;

    public String name;
    public int userCount;
    public int iconResId;
    public boolean isJoined;

    public Channel(@NonNull String id, String name, int userCount, int iconResId, boolean isJoined) {
        this.id = id;
        this.name = name;
        this.userCount = userCount;
        this.iconResId = iconResId;
        this.isJoined = isJoined;
    }
}
