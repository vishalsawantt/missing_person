package com.example.missingperson;// MessageAdapter.java
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.Timestamp;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {
    private List<Message> messageList;
    private String currentUserUid; // Current user's UID

    // Constructor
    public MessageAdapter(List<Message> messageList, String currentUserUid) {
        this.messageList = messageList;
        this.currentUserUid = currentUserUid;

    }



    @NonNull
    @Override
    public MessageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_message, parent, false);
        return new MessageViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull MessageViewHolder holder, int position) {
        Message message = messageList.get(position);
        holder.tvMessage.setText(message.getMessage());

        // Format the timestamp
        Timestamp timestamp = message.getTimestamp();
        if (timestamp != null) {
            Date date = timestamp.toDate();
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy, HH:mm");
            String formattedDate = sdf.format(date);
            holder.tvTimestamp.setText(formattedDate);
        }

        // Determine if the message is sent by the current user or received from another user
        boolean isSender = message.getSenderId().equals(currentUserUid);

        // Set background color based on sender or receiver
        if (isSender) {
            holder.itemView.setBackgroundResource(R.drawable.sender_message_background); // Example background drawable for sender
        } else {
            holder.itemView.setBackgroundResource(R.drawable.receiver_message_background); // Example background drawable for receiver
        }
    }

    @Override
    public int getItemCount() {
        return messageList.size();
    }

    // ViewHolder class
    static class MessageViewHolder extends RecyclerView.ViewHolder {
        TextView tvMessage;
        TextView tvTimestamp;

        public MessageViewHolder(@NonNull View itemView) {
            super(itemView);
            tvMessage = itemView.findViewById(R.id.txtMessage);
            tvTimestamp = itemView.findViewById(R.id.tvTimestamp);
        }
    }
}
