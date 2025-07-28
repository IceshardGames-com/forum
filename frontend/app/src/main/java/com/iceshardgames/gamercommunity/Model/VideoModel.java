package com.iceshardgames.gamercommunity.Model;

public class VideoModel {
    private String title;
    private String videoUrl; // Stores the video URL/path

    public VideoModel(String title, String videoUrl) {
        this.title = title;
        this.videoUrl = videoUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getVideoUrl() {
        return videoUrl;
    }
}

