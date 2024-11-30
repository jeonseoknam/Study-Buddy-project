package com.example.studybuddy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class ScheduleAdapter extends RecyclerView.Adapter<ScheduleAdapter.ViewHolder> {
    private List<ScheduleModel> scheduleList;

    public ScheduleAdapter(List<ScheduleModel> scheduleList) {
        this.scheduleList = scheduleList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleModel schedule = scheduleList.get(position);

        holder.titleTextView.setText(schedule.getTitle());
        holder.dateTextView.setText(schedule.getDate());
        holder.timeTextView.setText(schedule.getTime());
        holder.nameTextView.setText(schedule.getName());
        Glide.with(holder.itemView.getContext())
                .load(schedule.getProfileUrl())
                .into(holder.profileImageView);
    }

    @Override
    public int getItemCount() {
        return scheduleList.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, dateTextView, timeTextView, nameTextView;
        ImageView profileImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            dateTextView = itemView.findViewById(R.id.date_text_view);
            timeTextView = itemView.findViewById(R.id.time_text_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
        }
    }
}
