package com.iceshardgames.gamercommunity.Model;


public class TrendingTopic {
    private String title;
    private String searches;
    private String trendDirection;
    private String category; // e.g., "Games", "Posts", "Users"

    public TrendingTopic(String title, String searches, String trendDirection, String category) {
        this.title = title;
        this.searches = searches;
        this.trendDirection = trendDirection;
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public String getSearches() {
        return searches;
    }

    public String getTrendDirection() {
        return trendDirection;
    }

    public String getCategory() {
        return category;
    }
}

