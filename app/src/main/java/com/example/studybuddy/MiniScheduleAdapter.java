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

public class MiniScheduleAdapter extends RecyclerView.Adapter<MiniScheduleAdapter.ViewHolder> {
    private List<ScheduleModel> schedules;

    public MiniScheduleAdapter(List<ScheduleModel> schedules) {
        this.schedules = schedules;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_mini_schedule, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ScheduleModel schedule = schedules.get(position);
        holder.titleTextView.setText(schedule.getTitle());
        holder.timeTextView.setText(schedule.getTime());
        holder.nameTextView.setText(schedule.getName());

        Glide.with(holder.profileImageView.getContext())
                .load(schedule.getProfileUrl())
                .circleCrop()
                .into(holder.profileImageView);
    }

    @Override
    public int getItemCount() {
        return schedules.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleTextView, timeTextView, nameTextView;
        ImageView profileImageView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.title_text_view);
            timeTextView = itemView.findViewById(R.id.time_text_view);
            nameTextView = itemView.findViewById(R.id.name_text_view);
            profileImageView = itemView.findViewById(R.id.profile_image_view);
        }
    }
}
