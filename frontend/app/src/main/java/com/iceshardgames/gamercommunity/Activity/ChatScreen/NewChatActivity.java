package com.iceshardgames.gamercommunity.Activity.ChatScreen;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.iceshardgames.gamercommunity.Adapter.UserAdapter;
import com.iceshardgames.gamercommunity.Model.UserItem;
import com.iceshardgames.gamercommunity.R;
import com.iceshardgames.gamercommunity.databinding.ActivityNewChatBinding;

import java.util.ArrayList;
import java.util.List;

public class NewChatActivity extends AppCompatActivity {

    ActivityNewChatBinding binding;
    private UserAdapter userAdapter;
    private List<UserItem> allUsersList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityNewChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        binding.userListRecycler.setLayoutManager(new LinearLayoutManager(this));

        // Load dummy user list
        allUsersList = new ArrayList<>();
        allUsersList.add(new UserItem("ShadowWolf", R.drawable.profilepic));
        allUsersList.add(new UserItem("PixelBlaster", R.drawable.profilepic));
        allUsersList.add(new UserItem("NoScopeKing", R.drawable.profilepic));
        allUsersList.add(new UserItem("LunaSniper", R.drawable.profilepic));

        userAdapter = new UserAdapter(getApplicationContext(), allUsersList);
        binding.userListRecycler.setAdapter(userAdapter);

        binding.searchUsers.addTextChangedListener(new TextWatcher() {
            @Override public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }
            @Override public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterUsers(s.toString());
            }
            @Override public void afterTextChanged(Editable s) {}
        });
    }

    private void filterUsers(String query) {
        List<UserItem> filtered = new ArrayList<>();
        for (UserItem user : allUsersList) {
            if (user.getName().toLowerCase().contains(query.toLowerCase())) {
                filtered.add(user);
            }
        }
        userAdapter = new UserAdapter(getApplicationContext(), filtered);
        binding.userListRecycler.setAdapter(userAdapter);
    }
}