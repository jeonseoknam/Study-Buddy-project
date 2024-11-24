package com.example.studybuddy;

import static java.security.AccessController.getContext;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.List;

public class TimeListAdapter extends RecyclerView.Adapter<TimeListAdapter.ViewHolder> {

    private List<StudySession> timeList;
    private FirebaseFirestore firestore;

    public TimeListAdapter(List<StudySession> timeList, FirebaseFirestore firestore) {
        this.timeList = timeList;
        this.firestore = firestore;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_time, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        StudySession session = timeList.get(position);
        holder.timeTextView.setText(session.getElapsed_time());

        // 시간 및 과목 표시
        holder.timeTextView.setText(session.getElapsed_time());
        holder.subjectTextView.setText("과목: " + session.getSubject_name());

        // 삭제 버튼 동작
        holder.deleteButton.setOnClickListener(v -> {
            String documentId = session.getId();
            firestore.collection("study_sessions").document(documentId)
                    .delete()
                    .addOnSuccessListener(aVoid -> {
                        Toast.makeText(holder.itemView.getContext(), "삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        timeList.remove(position);
                        notifyItemRemoved(position);
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(holder.itemView.getContext(), "삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });
    }

    @Override
    public int getItemCount() {
        return timeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView timeTextView;
        TextView subjectTextView;
        Button deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.time_text_view);
            subjectTextView = itemView.findViewById(R.id.subject_text_view);
            deleteButton = itemView.findViewById(R.id.delete_button);
        }
    }


}
