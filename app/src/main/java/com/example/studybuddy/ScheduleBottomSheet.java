package com.example.studybuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ScheduleBottomSheet extends BottomSheetDialogFragment {
    private static final String ARG_DATE = "selected_date";
    private String selectedDate;
    private String chatRoomId = "qwerqwer"; // Firestore에서 사용할 채팅방 ID (예: 채팅방 고유값)
    private FirebaseFirestore db;

    public static ScheduleBottomSheet newInstance(String date) {
        ScheduleBottomSheet fragment = new ScheduleBottomSheet();
        Bundle args = new Bundle();
        args.putString(ARG_DATE, date);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            selectedDate = getArguments().getString(ARG_DATE);
        }
        db = FirebaseFirestore.getInstance(); // Firestore 인스턴스 초기화
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.bottom_sheet_schedule, container, false);

        // 선택된 날짜 표시
        TextView dateTextView = view.findViewById(R.id.selected_date);
        dateTextView.setText("선택된 날짜: " + selectedDate);

        // 일정 입력 필드
        EditText scheduleEditText = view.findViewById(R.id.input_schedule);

        // 등록 버튼
        Button confirmButton = view.findViewById(R.id.btn_save_schedule);
        confirmButton.setOnClickListener(v -> {
            String scheduleTitle = scheduleEditText.getText().toString().trim();

            if (scheduleTitle.isEmpty()) {
                Toast.makeText(getContext(), "일정을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            saveScheduleToFirestore(scheduleTitle);
        });

        // 취소 버튼
        Button cancelButton = view.findViewById(R.id.btn_cancel);
        cancelButton.setOnClickListener(v -> dismiss());

        return view;
    }

    private void saveScheduleToFirestore(String scheduleTitle) {
        // Firestore 경로 설정
        CollectionReference schedulesRef = db.collection("chatRoom")
                .document("singleChat")
                .collection(chatRoomId)
                .document("schedules")
                .collection("schedules");

        // Firestore에 저장할 데이터
        Map<String, Object> scheduleData = new HashMap<>();
        scheduleData.put("date", selectedDate);
        scheduleData.put("title", scheduleTitle);
        scheduleData.put("time", "10:00"); // 기본 시간 설정
        scheduleData.put("description", "일정 등록 완료"); // 설명 추가
        scheduleData.put("createdBy", "userId1"); // 작성자 ID (사용자 ID로 교체)

        // Firestore에 데이터 추가
        schedulesRef.add(scheduleData)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "일정 등록 성공!", Toast.LENGTH_SHORT).show();
                    dismiss(); // 모달 닫기
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "일정 등록 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
