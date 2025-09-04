package com.iceshardgames.gamercommunity.Model;


public class ForumModel {
    private String id;
    private String title;
    private String stats;
    private String lastActive;
    private String status;
    private String category;
    private int imageResId;
    private String postPermission;  // ✅ NEW FIELD

    // Default constructor needed for Gson
    public ForumModel() {}
    public ForumModel(String id, String title, String stats, String lastActive, String status, String category, int imageResId, String postPermission) {
        this.id = id;
        this.title = title;
        this.stats = stats;
        this.lastActive = lastActive;
        this.status = status;
        this.category = category;
        this.imageResId = imageResId;
        this.postPermission = postPermission;
    }
    public String getPostPermission() { return postPermission; }

    public String getId() {
        return id;
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

