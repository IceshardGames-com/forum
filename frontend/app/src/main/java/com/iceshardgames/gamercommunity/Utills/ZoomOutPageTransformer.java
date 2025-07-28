package com.iceshardgames.gamercommunity.Utills;

import android.view.View;

import androidx.annotation.NonNull;
import androidx.viewpager2.widget.ViewPager2;

public class ZoomOutPageTransformer implements ViewPager2.PageTransformer {
    public void transformPage(@NonNull View page, float position) {
        float scale = Math.max(0.85f, 1 - Math.abs(position));
        page.setScaleY(scale);
        page.setAlpha(scale);
        page.setTranslationX(-position * page.getWidth() * 0.2f);
    }
}

