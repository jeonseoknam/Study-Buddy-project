package com.example.studybuddy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class ChatTimerRankingAdapter extends RecyclerView.Adapter<ChatTimerRankingAdapter.ViewHolder> {

    private final List<ChatTimerRankingItem> rankingList;

    public ChatTimerRankingAdapter(List<ChatTimerRankingItem> rankingList) {
        this.rankingList = rankingList;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_chat_timer_ranking, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ChatTimerRankingItem item = rankingList.get(position);
        holder.rankText.setText(String.valueOf(position + 1));
        holder.nicknameText.setText(item.getNickname());
        holder.totalTimeText.setText(formatTime(item.getTotalTime()));
    }

    @Override
    public int getItemCount() {
        return rankingList.size();
    }

    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;
        return String.format("%02d시간 %02d분 %02d초", hours, minutes, secs);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView rankText, nicknameText, totalTimeText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            rankText = itemView.findViewById(R.id.rank_text);
            nicknameText = itemView.findViewById(R.id.nickname_text);
            totalTimeText = itemView.findViewById(R.id.total_time_text);
        }
    }
}
