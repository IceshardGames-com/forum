package com.iceshardgames.gamercommunity.Fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.databinding.ActivityChatListBinding;

public class ChatListFragment extends Fragment {

    private ActivityChatListBinding binding;
    private enum Tab { MESSAGES, FRIENDS, REQUESTS }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = ActivityChatListBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        setTabSelected(binding.tabMessages);
        switchTab(Tab.MESSAGES);

        View.OnClickListener tabClickListener = v -> {
            clearTabSelection();
            setTabSelected((TextView) v);
            if (v.getId() == R.id.tab_messages)      switchTab(Tab.MESSAGES);
            else if (v.getId() == R.id.tab_threads)  switchTab(Tab.FRIENDS);
            else if (v.getId() == R.id.tab_requests) switchTab(Tab.REQUESTS);
        };

        binding.tabMessages.setOnClickListener(tabClickListener);
        binding.tabThreads.setOnClickListener(tabClickListener);
        binding.tabUnread.setVisibility(View.GONE);
        binding.tabRequests.setOnClickListener(tabClickListener);

        binding.fabNewChat.setOnClickListener(v ->
                getChildFragmentManager().setFragmentResult("fab_click", new Bundle()));
    }

    private void switchTab(Tab tab) {
        Fragment child;
        switch (tab) {
            case FRIENDS:
                child = new FriendsTabFragment();
                binding.fabNewChat.setVisibility(View.GONE);
                break;
            case REQUESTS:
                child = new RequestsTabFragment();
                binding.fabNewChat.setVisibility(View.GONE);
                break;
            case MESSAGES:
            default:
                child = new MessagesTabFragment();
//                binding.fabNewChat.setVisibility(View.VISIBLE);
                binding.fabNewChat.setVisibility(View.GONE);
                break;
        }

        getChildFragmentManager()
                .beginTransaction()
                .setReorderingAllowed(true)
                .replace(R.id.chat_content_container, child, tab.name())
                .commit();
    }

    private void clearTabSelection() {
        binding.tabMessages.setSelected(false);
        binding.tabThreads.setSelected(false);
        binding.tabRequests.setSelected(false);
    }

    private void setTabSelected(TextView tv) { tv.setSelected(true); }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
