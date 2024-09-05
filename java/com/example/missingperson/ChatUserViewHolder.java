package com.example.missingperson;

import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class ChatUserViewHolder extends RecyclerView.ViewHolder {
    TextView textViewUserName;

    public ChatUserViewHolder(@NonNull View itemView) {
        super(itemView);
        textViewUserName = itemView.findViewById(R.id.textViewUserName);
    }
}
