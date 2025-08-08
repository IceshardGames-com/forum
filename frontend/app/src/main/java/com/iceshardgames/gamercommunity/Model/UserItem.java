package com.iceshardgames.gamercommunity.Model;

public class UserItem {
    private String name;
    private int avatarResId;

    public UserItem(String name, int avatarResId) {
        this.name = name;
        this.avatarResId = avatarResId;
    }

    public String getName() { return name; }
    public int getAvatarResId() { return avatarResId; }
}

