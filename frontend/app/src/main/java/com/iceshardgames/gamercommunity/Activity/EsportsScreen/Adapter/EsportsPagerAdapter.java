package com.iceshardgames.gamercommunity.Activity.EsportsScreen.Adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.iceshardgames.gamercommunity.Activity.EsportsScreen.Fragment.CompletedTabFragment;
import com.iceshardgames.gamercommunity.Activity.EsportsScreen.Fragment.LiveTabFragment;
import com.iceshardgames.gamercommunity.Activity.EsportsScreen.Fragment.TeamsTabFragment;
import com.iceshardgames.gamercommunity.Activity.EsportsScreen.Fragment.UpcomingTabFragment;

public class EsportsPagerAdapter  extends FragmentStateAdapter {

    public EsportsPagerAdapter(@NonNull Fragment fragment) {
        super(fragment);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position) {
            case 0: return new LiveTabFragment();
            case 1: return new UpcomingTabFragment();
            case 2: return new CompletedTabFragment();
            case 3: return new TeamsTabFragment();
            default: return new LiveTabFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 4;
    }
}
