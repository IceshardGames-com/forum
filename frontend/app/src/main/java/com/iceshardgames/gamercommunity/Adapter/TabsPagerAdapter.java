package com.iceshardgames.gamercommunity.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.iceshardgames.gamercommunity.Fragment.HomeScreen.ForumsFragment;
import com.iceshardgames.gamercommunity.Fragment.HomeScreen.GamesFragment;
import com.iceshardgames.gamercommunity.Fragment.HomeScreen.TrendingFragment;

public class TabsPagerAdapter extends FragmentStateAdapter {

    public TabsPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 1: return new GamesFragment();
            case 2: return new ForumsFragment();
            default: return new TrendingFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 3;
    }
}