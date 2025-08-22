package com.iceshardgames.gamercommunity.Adapter;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.iceshardgames.gamercommunity.DB.FriendRequest;
import com.iceshardgames.gamercommunity.R;

import java.util.List;
import java.util.function.Consumer;

public class FriendRequestAdapter extends RecyclerView.Adapter<FriendRequestAdapter.ViewHolder> {

    private Context context;
    private List<FriendRequest> requests;
    private Consumer<FriendRequest> onAccept, onDecline, onBlock;

    public FriendRequestAdapter(Context context, List<FriendRequest> requests,
                                Consumer<FriendRequest> onAccept,
                                Consumer<FriendRequest> onDecline,
                                Consumer<FriendRequest> onBlock) {
        this.context = context;
        this.requests = requests;
        this.onAccept = onAccept;
        this.onDecline = onDecline;
        this.onBlock = onBlock;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_friend_request, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FriendRequest request = requests.get(position);
        holder.name.setText(request.getSenderName());

        holder.accept.setOnClickListener(v -> onAccept.accept(request));
        holder.decline.setOnClickListener(v -> onDecline.accept(request));
        holder.block.setOnClickListener(v -> onBlock.accept(request));
    }

    @Override
    public int getItemCount() {
        return requests.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView name;
        TextView accept, decline, block;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.friendName);
            accept = itemView.findViewById(R.id.btnAccept);
            decline = itemView.findViewById(R.id.btnDecline);
            block = itemView.findViewById(R.id.btnBlock);
        }
    }
}
