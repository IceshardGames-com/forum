package com.iceshardgames.gamercommunity.Activity.MainScreen;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class InboxPagerAdapter extends FragmentStateAdapter {

    public InboxPagerAdapter(@NonNull FragmentActivity fa) {
        super(fa);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        return position == 0 ? new NotificationsFragment() : new MessagesFragment();
    }

    @Override
    public int getItemCount() {
        return 2;
    }
}
