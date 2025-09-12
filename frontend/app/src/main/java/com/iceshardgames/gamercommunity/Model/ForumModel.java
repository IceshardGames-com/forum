package com.iceshardgames.gamercommunity.Model;


public class ForumModel {
    private String id;
    private String title;
    private String stats;
    private String lastActive;
    private String status;
    private String category;
    private String owner;
    private int imageResId;
    private String postPermission;  // ✅ NEW FIELD
    // inside ForumModel
    private String slug; // add to fields & constructor

    public String getSlug() {
        return slug;
    }
    public void setSlug(String slug) {
        this.slug = slug;
    }
    // Default constructor needed for Gson
    public ForumModel() {}
    public ForumModel(String id, String title, String stats, String lastActive, String status, String category, int imageResId, String postPermission, String owner, String slug) {
        this.id = id;
        this.title = title;
        this.stats = stats;
        this.lastActive = lastActive;
        this.status = status;
        this.category = category;
        this.imageResId = imageResId;
        this.postPermission = postPermission;
        this.owner = owner;
        this.slug = slug;
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
    public String getOwner() {
        return owner;
    }

    public int getImageResId() {
        return imageResId;
    }
}

