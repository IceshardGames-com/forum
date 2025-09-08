package com.iceshardgames.gamercommunity.Utills;

import android.content.Context;
import android.content.SharedPreferences;

public class SessionManager {
    private static final String PREF_NAME = "user_session";
    private static final String KEY_USER_ID = "user_id";
    private static volatile SessionManager INSTANCE;
    private volatile String accessToken; // just the raw JWT, no "Bearer "

    private SessionManager() {}

    public static SessionManager get() {
        if (INSTANCE == null) {
            synchronized (SessionManager.class) {
                if (INSTANCE == null) INSTANCE = new SessionManager();
            }
        }
        return INSTANCE;
    }

    public void setAccessToken(String token) { this.accessToken = token; }
    public String getAccessToken() { return accessToken; }
    public static void saveUserId(Context context, String userId) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_USER_ID, userId).apply();
    }

    public static String getUserId(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_USER_ID, null);
    }

    public static void clearSession(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().clear().apply();
    }
}
