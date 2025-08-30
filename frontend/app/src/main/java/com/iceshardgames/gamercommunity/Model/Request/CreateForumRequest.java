package com.iceshardgames.gamercommunity.Model.Request;

public class CreateForumRequest {
    private String name;
    private String slug;
    private String description;
    private boolean verified;
    private String postPermission;

    public CreateForumRequest(String name, String slug, String description, boolean verified, String postPermission) {
        this.name = name;
        this.slug = slug;
        this.description = description;
        this.verified = verified;
        this.postPermission = postPermission;
    }
}
