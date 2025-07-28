package com.iceshardgames.gamercommunity.Activity.MainScreen;

import android.os.Bundle;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.iceshardgames.gamercommunity.R;

public class NotificationsFragment extends Fragment {

    public NotificationsFragment() {}

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_notifications, container, false);

        RecyclerView recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        NotificationAdapter adapter = new NotificationAdapter();
        adapter.setOnItemClickListener((position, data) -> {
            // Handle each item click here
            Toast.makeText(getContext(), "Clicked: " + data, Toast.LENGTH_SHORT).show();

            // Example: open detail screen or perform action
            if (data.contains("Needafriend")) {
                // Navigate or show dialog
            }
        });


        recyclerView.setAdapter(adapter);

        return view;
    }
}
