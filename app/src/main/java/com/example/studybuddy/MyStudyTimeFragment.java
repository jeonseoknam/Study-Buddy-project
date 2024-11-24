package com.example.studybuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MyStudyTimeFragment extends Fragment {

    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    private RecyclerView recyclerView;
    private StudyTimeAdapter adapter;
    private List<StudyTimeItem> subjectStudyTimes = new ArrayList<>();

    private TextView totalTimeText; // 총 공부 시간 표시 TextView


    public static MyStudyTimeFragment newInstance(String param1, String param2) {
        MyStudyTimeFragment fragment = new MyStudyTimeFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_study_time, container, false);

        // Firebase 초기화
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();

        // View 초기화
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        totalTimeText = view.findViewById(R.id.total_study_time); // 총 공부 시간 TextView

        // 데이터 가져오기 및 화면 업데이트
        fetchStudySessions();

        return view;
    }

    private void fetchStudySessions() {
        String userId = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : "anonymous";

        CollectionReference studySessionsRef = firestore.collection("study_sessions");
        studySessionsRef
                .whereEqualTo("user_id", userId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Long> subjectTimes = new HashMap<>();
                    long totalTimeInSeconds = 0;

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        StudySession session = document.toObject(StudySession.class);

                        // 과목별 시간 계산
                        String subjectName = session.getSubject_name();
                        String[] timeParts = session.getElapsed_time().split(":"); // "mm:ss" 형식
                        long sessionTimeInSeconds = Integer.parseInt(timeParts[0]) * 60 // mm -> 초
                                + Integer.parseInt(timeParts[1]);     // ss -> 초

                        totalTimeInSeconds += sessionTimeInSeconds;

                        if (subjectTimes.containsKey(subjectName)) {
                            subjectTimes.put(subjectName, subjectTimes.get(subjectName) + sessionTimeInSeconds);
                        } else {
                            subjectTimes.put(subjectName, sessionTimeInSeconds);
                        }
                    }

                    // 총 공부 시간 업데이트
                    totalTimeText.setText(formatTime(totalTimeInSeconds));

                    // 과목별 시간 리스트로 변환
                    subjectStudyTimes.clear();
                    for (Map.Entry<String, Long> entry : subjectTimes.entrySet()) {
                        subjectStudyTimes.add(new StudyTimeItem(entry.getKey(), entry.getValue()));
                    }

                    // RecyclerView Adapter 업데이트
                    adapter = new StudyTimeAdapter(subjectStudyTimes);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "데이터를 불러오지 못했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    // 시간 포맷팅 메서드 (초 -> "hh:mm:ss")
    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("총 공부 시간: %02d시간 %02d분 %02d초", hours, minutes, secs);
    }
}
