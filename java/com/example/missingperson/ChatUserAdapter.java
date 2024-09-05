package com.example.missingperson;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

import de.hdodenhof.circleimageview.CircleImageView;

public class ChatUserAdapter extends RecyclerView.Adapter<ChatUserAdapter.ChatUserViewHolder> {

    private List<ChatUser> chatUserList;
    private Context context;

    public ChatUserAdapter(List<ChatUser> chatUserList) {
        this.chatUserList = chatUserList;
    }

    @NonNull
    @Override
    public ChatUserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        context = parent.getContext(); // Get context from parent view
        View view = LayoutInflater.from(context).inflate(R.layout.item_chat_user, parent, false);
        return new ChatUserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ChatUserViewHolder holder, int position) {
        ChatUser chatUser = chatUserList.get(position);
        holder.textViewUserName.setText(chatUser.getFullName());

        // Load image using Picasso
        Picasso.get().load(chatUser.getImageUrl()).placeholder(R.drawable.default_profile).into(holder.userImage);

        // Handle item click
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(context, ChatActivity.class);
                intent.putExtra("receiverUid", chatUser.getUserId());
                intent.putExtra("receiverName", chatUser.getFullName());  // Pass the full name
                context.startActivity(intent);
            }
        });
    }


    @Override
    public int getItemCount() {
        return chatUserList.size();
    }

    static class ChatUserViewHolder extends RecyclerView.ViewHolder {
        TextView textViewUserName;
        CircleImageView userImage;  // Add this line

        ChatUserViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewUserName = itemView.findViewById(R.id.textViewUserName);
            userImage = itemView.findViewById(R.id.userImage);  // Add this line
        }
    }
}
