package com.iceshardgames.gamercommunity.Utills;

import android.content.Context;
import android.content.SharedPreferences;

public class SharedPrefManager {
    private static final String SHARED_PREF_NAME = "vrnexus_pref";
    private static final String KEY_EMAIL = "user_email";
    private static final String KEY_USERNAME = "username";
    public static void saveEmail(Context context, String email) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString(KEY_EMAIL, email).apply();
    }

    public static String getEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(KEY_EMAIL, null);
    }

    public static void clearEmail(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        prefs.edit().remove(KEY_EMAIL).apply();
    }

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;

    public SharedPrefManager(Context context) {
        sharedPreferences = context.getSharedPreferences(SHARED_PREF_NAME, Context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public void saveUser(String username) {
        editor.putString(KEY_USERNAME, username);
        editor.apply();
    }

    public String getUsername() {
        return sharedPreferences.getString(KEY_USERNAME, null);
    }
}
