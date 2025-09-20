package com.iceshardgames.gamercommunity.Model;


import android.util.Log;

import com.google.gson.annotations.SerializedName;

import java.text.ParsePosition;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

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
    @SerializedName("createdAt")

    private String createdAt; // new: ISO 8601 timestamp from server e.g. "2025-09-19T10:17:02.345Z"
    // in ForumModel.java
    private boolean localNew = false; // default false

    public boolean isLocalNew() { return localNew; }
    public void setLocalNew(boolean localNew) { this.localNew = localNew; }

    public ForumModel(String id, String title, String stats, String lastActive, String status, String category, int imageResId, String postPermission, String owner, String slug, String createdAt) {
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
        this.createdAt = createdAt;
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

    // inside ForumModel

    // keep other existing getters/setters...
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }

    /**
     * Returns true if createdAt is within the last 24 hours.
     * Uses java.time API (Android API 26+). If createdAt is null/invalid => false.
     */
    // isNew() using java.time
    public boolean isNew() {
        if (this.localNew) return true;
        if (this.createdAt == null || this.createdAt.trim().isEmpty()) return false;

        // 1) Try java.time Instant (fast & robust) if available
        try {
            Instant createdInstant = Instant.parse(this.createdAt);
            long diffMs = System.currentTimeMillis() - createdInstant.toEpochMilli();
            return diffMs >= 0 && diffMs < 24L * 60L * 60L * 1000L;
        } catch (Exception ignore) {
            // fall through to legacy parsing
        }

        // 2) Try few common ISO formats (with milliseconds, without millis, timezone variants)
        List<String> patterns = Arrays.asList(
                "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",   // 2025-09-19T10:17:02.345Z
                "yyyy-MM-dd'T'HH:mm:ss.SSSX",     // 2025-09-19T10:17:02.345+00:00
                "yyyy-MM-dd'T'HH:mm:ss'Z'",       // 2025-09-19T10:17:02Z
                "yyyy-MM-dd'T'HH:mm:ssX"          // 2025-09-19T10:17:02+00:00
        );

        for (String p : patterns) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat(p, Locale.US);
                sdf.setLenient(true);
                // For patterns using literal 'Z' we must set timezone UTC
                if (p.contains("'Z'") || p.endsWith("X")) {
                    sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
                }
                ParsePosition pos = new ParsePosition(0);
                Date created = sdf.parse(this.createdAt, pos);
                if (created != null) {
                    long diffMs = System.currentTimeMillis() - created.getTime();
                    return diffMs >= 0 && diffMs < 24L * 60L * 60L * 1000L;
                }
            } catch (Exception e) {
                // try next pattern
            }
        }

        // last resort: try to parse only date portion
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd", Locale.US);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));
            Date d = sdf.parse(this.createdAt);
            if (d != null) {
                long diffMs = System.currentTimeMillis() - d.getTime();
                return diffMs >= 0 && diffMs < 24L * 60L * 60L * 1000L;
            }
        } catch (Exception e) {
            Log.w("ForumModel", "isNew final parse failed for createdAt=" + this.createdAt);
        }

        return false;
    }

}

