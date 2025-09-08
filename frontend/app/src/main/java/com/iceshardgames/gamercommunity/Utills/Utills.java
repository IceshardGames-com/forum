package com.iceshardgames.gamercommunity.Utills;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.text.TextPaint;
import android.util.Log;
import android.widget.TextView;

import androidx.fragment.app.FragmentActivity;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.iceshardgames.gamercommunity.APIintegration.ApiClient;
import com.iceshardgames.gamercommunity.APIintegration.ApiService;
import com.iceshardgames.gamercommunity.Activity.ChatScreen.DeviceManager;
import com.iceshardgames.gamercommunity.Model.ForumModel;
import com.iceshardgames.gamercommunity.Model.Request.DeviceRegistrationRequest;
import com.iceshardgames.gamercommunity.Model.Response.DevicesResponse;
import com.iceshardgames.gamercommunity.R;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Response;

public class Utills {

    public static Dialog loadingDialog;
    public static void showLoadingDialog(Activity Activity) {
        loadingDialog = new Dialog(Activity);
        loadingDialog.setContentView(R.layout.loading_dialog);
        loadingDialog.setCancelable(false);
        loadingDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        loadingDialog.show();
    }

    public static void hideLoadingDialog() {
        if (loadingDialog != null && loadingDialog.isShowing()) {
            loadingDialog.dismiss();
        }
    }

    public static void GradientText(TextView screenTitleNav) {
        TextPaint paint = screenTitleNav.getPaint();
        float width = paint.measureText(screenTitleNav.getText().toString());
        Shader shader = new LinearGradient(
                0, 0, width, screenTitleNav.getTextSize(), // horizontal gradient
                new int[]{
                        Color.parseColor("#673373"), // Start color (orange)
                        Color.parseColor("#D76D77")  // End color (amber)
                },
                null,
                Shader.TileMode.CLAMP
        );
        paint.setShader(shader);
        screenTitleNav.invalidate();
    }

    // Utills.java
    public static String getUserId(Context context) {
        // Implement your user ID retrieval logic here
        // For example from SharedPreferences
        return "user_" + System.currentTimeMillis(); // placeholder
    }

    public static String getUsername(Context context) {
        // Implement your username retrieval logic here
        return "User"; // placeholder
    }

    public static void registerDeviceAtLogin(Context ctx) {
        SharedPreferences sp = ctx.getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String raw = sp.getString("accessToken", "");
        if (raw == null || raw.isEmpty()) {
            Log.e("==namah", "No access token; skipping device registration");
            return;
        }
        String token = "Bearer " + raw;

        DeviceManager dm = new DeviceManager(ctx.getApplicationContext());
        DeviceRegistrationRequest body = new DeviceRegistrationRequest(
                dm.getOrCreateDeviceId(),
                dm.getPublicKey(),           // "" if not using E2E yet
                dm.getDeviceName(),
                "android"
        );

        ApiService api = ApiClient.getRetrofit().create(ApiService.class);
        Log.d("==namah", "registerDevice id= " + dm.getOrCreateDeviceId());
        Log.d("==namah", "getPublicKey id= " + dm.getPublicKey());
        Log.d("==namah", "getDeviceName id= " + dm.getDeviceName());

        api.registerDevice(token, body).enqueue(new retrofit2.Callback<DevicesResponse>() {
            @Override
            public void onResponse(Call<DevicesResponse> call, Response<DevicesResponse> r) {
                if (r.isSuccessful()) {
                    String registeredId = null;
                    if (registeredId == null || registeredId.isEmpty()) {
                        registeredId = dm.getOrCreateDeviceId();
                    }
                    Utills.saveDeviceId(ctx, registeredId);
                    Log.d("==namah", "Registered device, saved id=" + registeredId);
                } else {
                    Log.e("==namah", "Register failed code=" + r.code());
                }
            }

            @Override
            public void onFailure(Call<DevicesResponse> call, Throwable t) {
                Log.e("==namah", "Register error", t);
            }
        });
    }

    // Utills.java
    public static final String PREFS = "UserPrefs";
    private static final String KEY_DEVICE_ID = "deviceId";

    public static void saveDeviceId(Context ctx, String deviceId) {
        ctx.getSharedPreferences(PREFS, MODE_PRIVATE)
                .edit()
                .putString(KEY_DEVICE_ID, deviceId)
                .apply();
    }

    public static String readDeviceId(Context ctx) {
        return ctx.getSharedPreferences(PREFS, MODE_PRIVATE)
                .getString(KEY_DEVICE_ID, "");
    }

    public static long parseServerTimeToMillis(String iso) {
        if (iso == null) return 0L;
        try {
            if (android.os.Build.VERSION.SDK_INT >= 26) {
                return java.time.OffsetDateTime.parse(iso).toInstant().toEpochMilli();
            } else {
                // common ISO forms: 2025-09-04T05:21:33.123Z or without millis
                java.text.SimpleDateFormat sdf;
                if (iso.endsWith("Z")) {
                    // try with millis
                    try {
                        sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);
                        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                        return sdf.parse(iso).getTime();
                    } catch (Exception ignore) {
                        // fallback without millis
                        sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US);
                        sdf.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                        return sdf.parse(iso).getTime();
                    }
                } else {
                    // offset like +00:00
                    try {
                        sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX", java.util.Locale.US);
                        return sdf.parse(iso).getTime();
                    } catch (Exception e) {
                        sdf = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssXXX", java.util.Locale.US);
                        return sdf.parse(iso).getTime();
                    }
                }
            }
        } catch (Exception e) {
            return 0L;
        }
    }

    public static String getTimeAgo(long timeMillis) {
        if (timeMillis <= 0) return "";
        long diff = System.currentTimeMillis() - timeMillis;
        long sec = Math.max(1, diff / 1000); // avoid 0s
        long min = sec / 60, hr = min / 60, day = hr / 24, wk = day / 7;

        if (sec < 60) return sec + "s ago";
        if (min < 60) return min + "m ago";
        if (hr  < 24) return hr  + "h ago";
        if (day < 7)  return day + "d ago";
        return wk + "w ago";
    }


    public static long parseIso8601ToMillis(String iso) {
        if (iso == null || iso.isEmpty()) return 0L;

        // Works on all API levels if Java 8 desugaring is enabled (recommended)
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                return Instant.parse(iso).toEpochMilli();
            }
        } catch (Throwable ignore) {}

        // Fallback with millis
        try {
            java.text.SimpleDateFormat f = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", java.util.Locale.US);
            f.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
            return f.parse(iso).getTime();
        } catch (Exception e1) {
            // Fallback without millis
            try {
                java.text.SimpleDateFormat f2 = new java.text.SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", java.util.Locale.US);
                f2.setTimeZone(java.util.TimeZone.getTimeZone("UTC"));
                return f2.parse(iso).getTime();
            } catch (Exception e2) {
                return 0L;
            }
        }
    }
}
