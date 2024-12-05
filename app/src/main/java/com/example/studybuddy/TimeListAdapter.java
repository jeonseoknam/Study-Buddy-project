package com.example.studybuddy;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.content.SharedPreferences;
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

import java.util.List;

public class TimeListAdapter extends RecyclerView.Adapter<TimeListAdapter.ViewHolder> {

    private final List<StudySession> timeList;
    private final String chatRoomId;
    private final FirebaseFirestore firestore;
    private final Context context; // Context를 멤버 변수로 추가
    private SharedPreferences chatNamePref;


    public TimeListAdapter(List<StudySession> timeList, String chatRoomId, FirebaseFirestore firestore, Context context) {
        this.timeList = timeList;
        this.chatRoomId = chatRoomId;
        this.firestore = firestore;
        this.context = context; // 생성자에서 Context를 초기화
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
        holder.subjectTextView.setText("과목: " + session.getSubject_name());

        // SharedPreferences 사용
        SharedPreferences chatIdPref = context.getSharedPreferences("chatName", Context.MODE_PRIVATE);
        String savedChatName = chatIdPref.getString("Name", "defaultChatName");

        // 랭킹 등록 버튼
        holder.rankingRegisterButton.setOnClickListener(v -> {
            if (chatRoomId == null || chatRoomId.isEmpty()) {
                Toast.makeText(v.getContext(), "채팅방 ID가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }



            // Firestore 경로 설정
            CollectionReference rankingRef = firestore.collection("soongsil")
                    .document("chat")
                    .collection("chatRoom")
                    .document("singleChat")
                    .collection(savedChatName)
                    .document("timerRecords")
                    .collection("records");

            // 저장할 데이터 생성
            RankingRecord record = new RankingRecord(
                    session.getUser_id(),
                    session.getElapsed_time(),
                    session.getSubject_name(),
                    System.currentTimeMillis()
            );

            // Firestore에 데이터 추가
            rankingRef.add(record)
                    .addOnSuccessListener(documentReference -> {
                        Toast.makeText(v.getContext(), "랭킹에 성공적으로 등록되었습니다.", Toast.LENGTH_SHORT).show();
                    })
                    .addOnFailureListener(e -> {
                        Toast.makeText(v.getContext(), "랭킹 등록 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    });
        });

        // 삭제 버튼
        holder.deleteButton.setOnClickListener(v -> {
            if (session.getId() != null) {
                firestore.collection("study_sessions").document(session.getId())
                        .delete()
                        .addOnSuccessListener(aVoid -> {
                            timeList.remove(position);
                            notifyItemRemoved(position);
                            notifyItemRangeChanged(position, timeList.size());
                            Toast.makeText(v.getContext(), "기록이 삭제되었습니다.", Toast.LENGTH_SHORT).show();
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(v.getContext(), "기록 삭제 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            }
        });
    }

    @Override
    public int getItemCount() {
        return timeList.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private final TextView timeTextView;
        private final TextView subjectTextView;
        private final Button rankingRegisterButton;
        private final Button deleteButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            timeTextView = itemView.findViewById(R.id.time_text_view);
            subjectTextView = itemView.findViewById(R.id.subject_text_view);
            rankingRegisterButton = itemView.findViewById(R.id.ranking_register_button);
            deleteButton = itemView.findViewById(R.id.delete_button);

        }
    }

    // Firestore에 저장할 랭킹 기록 모델
    public static class RankingRecord {
        private String user_id;
        private String elapsed_time;
        private String subject_name;
        private long timestamp;

        public RankingRecord() {
        }

        public RankingRecord(String user_id, String elapsed_time, String subject_name, long timestamp) {
            this.user_id = user_id;
            this.elapsed_time = elapsed_time;
            this.subject_name = subject_name;
            this.timestamp = timestamp;
        }

        public String getUser_id() {
            return user_id;
        }

        public void setUser_id(String user_id) {
            this.user_id = user_id;
        }

        public String getElapsed_time() {
            return elapsed_time;
        }

        public void setElapsed_time(String elapsed_time) {
            this.elapsed_time = elapsed_time;
        }

        public String getSubject_name() {
            return subject_name;
        }

        public void setSubject_name(String subject_name) {
            this.subject_name = subject_name;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}
