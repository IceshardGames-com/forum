package com.iceshardgames.gamercommunity.Utills;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.graphics.drawable.ColorDrawable;
import android.text.TextPaint;
import android.widget.TextView;

import com.iceshardgames.gamercommunity.Activity.RegisterScreen.RegisterScreenActivity;
import com.iceshardgames.gamercommunity.R;

import java.util.concurrent.ExecutorService;

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
}
