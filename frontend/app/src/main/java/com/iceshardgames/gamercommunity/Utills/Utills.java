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
import android.text.TextPaint;
import android.util.Log;
import android.widget.TextView;

import com.iceshardgames.gamercommunity.APIintegration.ApiClient;
import com.iceshardgames.gamercommunity.APIintegration.ApiService;
import com.iceshardgames.gamercommunity.Activity.RegisterScreen.RegisterScreenActivity;
import com.iceshardgames.gamercommunity.Chat.DeviceManager;
import com.iceshardgames.gamercommunity.Model.DeviceRegistrationRequest;
import com.iceshardgames.gamercommunity.Model.DevicesResponse;
import com.iceshardgames.gamercommunity.R;

import java.util.concurrent.ExecutorService;

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

    public static void GradientText(TextView screenTitleNav)
    {
        TextPaint paint = screenTitleNav.getPaint();
        float width = paint.measureText(screenTitleNav.getText().toString());
        Shader shader = new LinearGradient(
                0, 0, width, screenTitleNav.getTextSize(), // horizontal gradient
                new int[] {
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
            @Override public void onResponse(Call<DevicesResponse> call, Response<DevicesResponse> r) {
                if (r.isSuccessful()) {
                    Log.d("==namah", "Registered device");
                } else {
                    Log.e("==namah", "Register failed code=" + r.code());
                }
            }
            @Override public void onFailure(Call<DevicesResponse> call, Throwable t) {
                Log.e("==namah", "Register error", t);
            }
        });
    }

}
