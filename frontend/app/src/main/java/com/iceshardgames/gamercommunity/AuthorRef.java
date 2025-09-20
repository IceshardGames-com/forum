package com.iceshardgames.gamercommunity;


public class AuthorRef {
    public String id;           // canonical id (from "id" or "_id" or string)
    public String username;     // optional
    public String displayName;  // optional

    public String getId() {
        return id;
    }

    public String getName() {
        if (username != null && !username.isEmpty()) return username;
        if (displayName != null && !displayName.isEmpty()) return displayName;
        return null;
    }
}

