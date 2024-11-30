package com.example.studybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatCalendarActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private RecyclerView selectedDateRecyclerView;
    private ScheduleAdapter scheduleAdapter;
    private List<ScheduleModel> scheduleList;
    private String selectedDate = null; // 초기값을 null로 설정
    private String chatRoomId = "qwerqwer"; // 채팅방 ID
    private String userName = "";
    private String userProfileUrl = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_calendar);

        db = FirebaseFirestore.getInstance();
        selectedDateRecyclerView = findViewById(R.id.selected_date_recycler_view);
        selectedDateRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        scheduleList = new ArrayList<>();
        scheduleAdapter = new ScheduleAdapter(scheduleList);
        selectedDateRecyclerView.setAdapter(scheduleAdapter);

        // 현재 사용자 UID 가져오기
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        db.collection("userInfo").document(currentUserId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        userName = documentSnapshot.getString("Name");
                        userProfileUrl = documentSnapshot.getString("ProfileImage");
                    } else {
                        Toast.makeText(this, "사용자 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> Toast.makeText(this, "오류 발생: " + e.getMessage(), Toast.LENGTH_SHORT).show());

        CalendarView calendarView = findViewById(R.id.calendar_view);

        // 날짜 선택 리스너
        calendarView.setOnDateChangeListener((view, year, month, dayOfMonth) -> {
            selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth;
            fetchSchedulesForSelectedDate(); // 선택된 날짜에 따라 일정 업데이트
        });

        // 채팅방 일정 등록 버튼
        Button registerScheduleButton = findViewById(R.id.register_schedule_button);
        registerScheduleButton.setOnClickListener(v -> showBottomSheetDialog());

        // 채팅방 일정 보기 버튼
        Button viewScheduleButton = findViewById(R.id.view_schedule_button);
        viewScheduleButton.setOnClickListener(v -> {
            Intent intent = new Intent(ChatCalendarActivity.this, ScheduleListActivity.class);
            intent.putExtra("chatRoomId", chatRoomId);
            startActivity(intent);
        });
    }

    private void showBottomSheetDialog() {
        BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(this);
        View bottomSheetView = getLayoutInflater().inflate(R.layout.bottom_sheet_schedule, null);
        bottomSheetDialog.setContentView(bottomSheetView);

        // 입력 필드와 TimePicker 초기화
        EditText inputSchedule = bottomSheetView.findViewById(R.id.input_schedule);
        TimePicker timePicker = bottomSheetView.findViewById(R.id.time_picker);
        Button saveButton = bottomSheetView.findViewById(R.id.btn_save_schedule);
        Button cancelButton = bottomSheetView.findViewById(R.id.btn_cancel);
        TextView selectedDateView = bottomSheetView.findViewById(R.id.selected_date);

        // 날짜 텍스트 업데이트
        selectedDateView.setText("선택된 날짜: " + selectedDate);

        // "등록" 버튼 클릭 리스너
        saveButton.setOnClickListener(v -> {
            String scheduleTitle = inputSchedule.getText().toString().trim();
            int hour = timePicker.getHour();
            int minute = timePicker.getMinute();
            String time = String.format("%02d:%02d", hour, minute);

            if (scheduleTitle.isEmpty()) {
                Toast.makeText(ChatCalendarActivity.this, "일정을 입력하세요!", Toast.LENGTH_SHORT).show();
                return;
            }

            saveScheduleToFirestore(scheduleTitle, time);
            bottomSheetDialog.dismiss();
        });

        // "취소" 버튼 클릭 리스너
        cancelButton.setOnClickListener(v -> bottomSheetDialog.dismiss());

        bottomSheetDialog.show();
    }

    private void saveScheduleToFirestore(String title, String time) {
        if (selectedDate == null) {
            Toast.makeText(this, "날짜를 선택한 후 일정을 등록하세요!", Toast.LENGTH_SHORT).show();
            return;
        }

        CollectionReference schedulesRef = db.collection("chatRoom")
                .document("singleChat")
                .collection(chatRoomId)
                .document("schedules")
                .collection("schedules");

        Map<String, Object> scheduleData = new HashMap<>();
        scheduleData.put("title", title);
        scheduleData.put("date", selectedDate);
        scheduleData.put("time", time);
        scheduleData.put("name", userName);
        scheduleData.put("profileUrl", userProfileUrl);

        schedulesRef.add(scheduleData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(ChatCalendarActivity.this, "일정 등록 완료!", Toast.LENGTH_SHORT).show();
                    fetchSchedulesForSelectedDate(); // 등록 후 업데이트
                })
                .addOnFailureListener(e -> Toast.makeText(this, "등록 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show());
    }

    private void fetchSchedulesForSelectedDate() {
        if (selectedDate == null) {
            scheduleList.clear(); // 날짜 선택 전에는 일정 비움
            scheduleAdapter.notifyDataSetChanged();
            return;
        }

        CollectionReference schedulesRef = db.collection("chatRoom")
                .document("singleChat")
                .collection(chatRoomId)
                .document("schedules")
                .collection("schedules");

        schedulesRef.whereEqualTo("date", selectedDate).get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    scheduleList.clear();
                    for (DocumentSnapshot document : queryDocumentSnapshots) {
                        String title = document.getString("title");
                        String time = document.getString("time");
                        String name = document.getString("name");
                        String profileUrl = document.getString("profileUrl");

                        scheduleList.add(new ScheduleModel(title, selectedDate, time, name, profileUrl));
                    }
                    scheduleAdapter.notifyDataSetChanged();
                })
                .addOnFailureListener(e -> Toast.makeText(this, "일정을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show());
    }
}
