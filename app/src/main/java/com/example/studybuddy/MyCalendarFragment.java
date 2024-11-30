package com.example.studybuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
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

public class MyCalendarFragment extends Fragment {

    private List<ScheduleModel> filteredSchedules = new ArrayList<>(); // 선택 날짜 필터링된 일정 리스트
    private ScheduleAdapter scheduleAdapter;
    private String selectedDate; // 선택한 날짜
    private FirebaseFirestore db;
    private CollectionReference userPrivateScheduleRef;
    private String userId;

    public static MyCalendarFragment newInstance() {
        return new MyCalendarFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_calendar, container, false);

        // Firebase 초기화
        db = FirebaseFirestore.getInstance();
        userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userPrivateScheduleRef = db.collection("userInfo").document(userId).collection("userPrivateSchedule");

        // View 초기화
        CalendarView calendarView = view.findViewById(R.id.calendar_view);
        RecyclerView scheduleRecyclerView = view.findViewById(R.id.schedule_recycler_view);
        EditText inputSchedule = view.findViewById(R.id.input_schedule);
        Button addScheduleButton = view.findViewById(R.id.register_button);

        // RecyclerView 설정
        scheduleRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        scheduleAdapter = new ScheduleAdapter(filteredSchedules);
        scheduleRecyclerView.setAdapter(scheduleAdapter);

        // 캘린더 날짜 선택 이벤트
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth; // 선택된 날짜 설정
            fetchSchedulesFromFirestore(); // Firestore에서 일정 불러오기
        });

        // 일정 추가 버튼 클릭 이벤트
        addScheduleButton.setOnClickListener(v -> {
            if (selectedDate == null) {
                Toast.makeText(getContext(), "날짜를 먼저 선택해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            String scheduleText = inputSchedule.getText().toString().trim();
            if (scheduleText.isEmpty()) {
                Toast.makeText(getContext(), "일정을 입력해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Firestore에 일정 저장
            saveScheduleToFirestore(scheduleText);
            inputSchedule.setText(""); // 입력 필드 초기화
        });

        return view;
    }

    // Firestore에서 일정 불러오기
    private void fetchSchedulesFromFirestore() {
        if (selectedDate == null) return;

        userPrivateScheduleRef.whereEqualTo("date", selectedDate).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        filteredSchedules.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            String description = document.getString("description");
                            String time = document.getString("time");
                            filteredSchedules.add(new ScheduleModel(description, selectedDate, time, "", ""));
                        }
                        scheduleAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(getContext(), "일정을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // Firestore에 일정 저장
    private void saveScheduleToFirestore(String scheduleText) {
        Map<String, Object> scheduleData = new HashMap<>();
        scheduleData.put("date", selectedDate);
        scheduleData.put("description", scheduleText);
        scheduleData.put("time", "시간 미정");

        userPrivateScheduleRef.add(scheduleData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "일정이 추가되었습니다.", Toast.LENGTH_SHORT).show();
                    fetchSchedulesFromFirestore(); // Firestore에서 일정 다시 불러오기
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "일정 추가 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
