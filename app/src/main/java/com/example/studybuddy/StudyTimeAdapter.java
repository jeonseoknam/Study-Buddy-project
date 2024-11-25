package com.example.studybuddy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class StudyTimeAdapter extends RecyclerView.Adapter<StudyTimeAdapter.ViewHolder> {

    private List<StudyTimeItem> subjectStudyTimes;

    public StudyTimeAdapter(List<StudyTimeItem> subjectStudyTimes) {
        this.subjectStudyTimes = subjectStudyTimes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_study_time, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudyTimeItem item = subjectStudyTimes.get(position);
        holder.subjectNameText.setText(item.getSubjectName());
        holder.elapsedTimeText.setText(formatTime(item.getElapsedTime()));
    }

    @Override
    public int getItemCount() {
        return subjectStudyTimes.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView subjectNameText;
        TextView elapsedTimeText;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            subjectNameText = itemView.findViewById(R.id.subject_name_text);
            elapsedTimeText = itemView.findViewById(R.id.elapsed_time_text);
        }
    }


    // 초 단위 -> "mm:ss" 형식 변환
    private String formatTime(long seconds) {
        long minutes = seconds / 60;        // 분 계산
        long secs = seconds % 60;          // 초 계산

        return String.format("%02d분 %02d초", minutes, secs); // "mm:ss" 형식
    }

}