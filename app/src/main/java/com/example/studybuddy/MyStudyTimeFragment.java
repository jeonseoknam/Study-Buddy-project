package com.example.studybuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
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

    // Firebase 관련 변수
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    // RecyclerView 및 Adapter
    private RecyclerView recyclerView;
    private StudyTimeAdapter adapter;
    private List<StudyTimeItem> subjectStudyTimes = new ArrayList<>();

    // UI 요소
    private TextView totalTimeText; // 총 공부 시간 표시 TextView
    private Spinner sortSpinner;   // 정렬 기준을 선택하는 Spinner

    // 현재 선택된 정렬 옵션
    private String currentSortOption = "1주"; // 기본 정렬 기준

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

        // UI 요소 초기화
        sortSpinner = view.findViewById(R.id.sort_spinner);
        totalTimeText = view.findViewById(R.id.total_study_time);
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // Spinner 설정
        ArrayAdapter<CharSequence> spinnerAdapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sort_options, android.R.layout.simple_spinner_item);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(spinnerAdapter);

        // Spinner 선택 이벤트 처리
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentSortOption = parent.getItemAtPosition(position).toString();
                fetchStudySessions(); // 선택된 기준에 따라 데이터를 다시 가져옴
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // 데이터 가져오기 및 초기 설정
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
                    long currentTime = System.currentTimeMillis();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        // 필드 데이터 가져오기
                        String subjectName = document.getString("subject_name");
                        String elapsedTime = document.getString("elapsed_time");
                        Object timestampObj = document.get("timestamp");

                        // 필드 검증
                        if (subjectName == null || elapsedTime == null || !(timestampObj instanceof Long)) {
                            continue;
                        }

                        long timestamp = (Long) timestampObj;

                        // 시간 필터링 (정렬 기준에 따른 데이터 필터링)
                        if (isWithinTimeRange(currentSortOption, timestamp, currentTime)) {
                            String[] timeParts = elapsedTime.split(":");
                            long sessionTimeInSeconds = Integer.parseInt(timeParts[0]) * 60
                                    + Integer.parseInt(timeParts[1]);

                            totalTimeInSeconds += sessionTimeInSeconds;

                            // 과목별 시간 누적
                            subjectTimes.put(subjectName, subjectTimes.getOrDefault(subjectName, 0L) + sessionTimeInSeconds);
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

    // 정렬 기준에 따른 시간 필터링
    private boolean isWithinTimeRange(String sortOption, long timestamp, long currentTime) {
        long oneDayInMillis = 24 * 60 * 60 * 1000;  // 하루
        long oneWeekInMillis = 7 * oneDayInMillis;  // 일주일
        long oneMonthInMillis = 30 * oneDayInMillis; // 한 달

        switch (sortOption) {
            case "하루":
                return currentTime - timestamp <= oneDayInMillis;
            case "1주":
                return currentTime - timestamp <= oneWeekInMillis;
            case "1달":
                return currentTime - timestamp <= oneMonthInMillis;
            default:
                return true;
        }
    }

    // 시간 포맷팅 메서드 (초 -> "hh:mm:ss")
    private String formatTime(long seconds) {
        long hours = seconds / 3600;
        long minutes = (seconds % 3600) / 60;
        long secs = seconds % 60;

        return String.format("나의 총 공부 시간: %02d시간 %02d분 %02d초", hours, minutes, secs);
    }
}
