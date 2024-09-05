package com.example.missingperson;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHolder> {
    private List<UserReport> userReports;
    private OnItemClickListener onItemClickListener;

    public UserAdapter(List<UserReport> userReports) {
        this.userReports = userReports;
    }

    public interface OnItemClickListener {
        void onItemClick(UserReport user, int position);
    }

    public void setOnItemClickListener(OnItemClickListener onItemClickListener) {
        this.onItemClickListener = onItemClickListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserReport userReport = userReports.get(position);
        holder.textViewName.setText(userReport.getName());

        // Load the image URL into the ImageView using Picasso
        Picasso.get()
                .load(userReport.getImageUrl())
                .into(holder.imageViewPerson);

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onItemClickListener != null) {
                    int currentPosition = holder.getAdapterPosition();
                    if (currentPosition != RecyclerView.NO_POSITION) {
                        onItemClickListener.onItemClick(userReports.get(currentPosition), currentPosition);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return userReports.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textViewName;
        ImageView imageViewPerson;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewName = itemView.findViewById(R.id.textViewName);
            imageViewPerson = itemView.findViewById(R.id.imageViewPerson);
        }
    }
}
