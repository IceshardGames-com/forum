package com.iceshardgames.gamercommunity.Model.Request;

public class CreatePostRequest {
    private String title;
    private String content;

    public CreatePostRequest(String title, String content) {
        this.title = title;
        this.content = content;
    }
}