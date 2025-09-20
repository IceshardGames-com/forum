package com.iceshardgames.gamercommunity;

import com.google.gson.annotations.SerializedName;

public class Author {
    @SerializedName("_id")
    public String _id;          // sometimes servers use _id
    public String id;          // also present in your example
    public String username;
    public String displayName;
    public Boolean isPrivileged; // can be null
}

