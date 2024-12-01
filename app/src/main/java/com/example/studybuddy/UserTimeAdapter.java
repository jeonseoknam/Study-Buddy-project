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

class UserTimerAdapter extends RecyclerView.Adapter<UserTimerAdapter.ViewHolder> {

    private List<UserTimerModel> userList;

    public UserTimerAdapter(List<UserTimerModel> userList) {
        this.userList = userList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.user_timer_item, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        UserTimerModel user = userList.get(position);
        holder.userName.setText(user.getUserName());
        // formatTime() 메서드가 long 값을 받아들일 수 있도록 수정
        holder.timerText.setText(formatTime(user.getElapsedTime()));

        // Glide를 사용해 프로필 이미지 로드
        Glide.with(holder.profileImage.getContext())
                .load(user.getProfileImage())
                .placeholder(R.drawable.ic_profile_placeholder)
                .into(holder.profileImage);
    }

    @Override
    public int getItemCount() {
        return userList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImage;
        TextView userName, timerText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImage = itemView.findViewById(R.id.profile_image);
            userName = itemView.findViewById(R.id.user_name);
            timerText = itemView.findViewById(R.id.timer_text);
        }
    }

    private String formatTime(long elapsedTime) {
        int hours = (int) (elapsedTime / 3600);
        int minutes = (int) ((elapsedTime % 3600) / 60);
        int seconds = (int) (elapsedTime % 60);

        return String.format("%02d:%02d:%02d", hours, minutes, seconds);
    }

}
