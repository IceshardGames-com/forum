package com.iceshardgames.gamercommunity.Fragment;


public class ForumModel {
    private String title;
    private String stats;
    private String lastActive;
    private String status;
    private String category;
    private int imageResId;

    public ForumModel(String title, String stats, String lastActive, String status, String category, int imageResId) {
        this.title = title;
        this.stats = stats;
        this.lastActive = lastActive;
        this.status = status;
        this.category = category;
        this.imageResId = imageResId;
    }

    public String getTitle() {
        return title;
    }

    public String getStats() {
        return stats;
    }

    public String getLastActive() {
        return lastActive;
    }

    public String getStatus() {
        return status;
    }

    public String getCategory() {
        return category;
    }

    public int getImageResId() {
        return imageResId;
    }
}

