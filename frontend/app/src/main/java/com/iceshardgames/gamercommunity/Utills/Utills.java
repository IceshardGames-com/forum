package com.iceshardgames.gamercommunity.Utills;

import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Shader;
import android.text.TextPaint;
import android.widget.TextView;

public class Utills {

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
}
