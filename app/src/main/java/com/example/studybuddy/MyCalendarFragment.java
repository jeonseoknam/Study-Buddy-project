package com.example.studybuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class MyCalendarFragment extends Fragment {

    private List<Schedule> scheduleList = new ArrayList<>();
    private String selectedDate;

    public static MyCalendarFragment newInstance() {
        return new MyCalendarFragment();
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_calendar, container, false);

        // View 참조
        CalendarView calendarView = view.findViewById(R.id.calendar_view);
        EditText inputSchedule = view.findViewById(R.id.input_schedule);
        Button registerButton = view.findViewById(R.id.register_button);
        LinearLayout confirmationButtons = view.findViewById(R.id.confirmation_buttons);
        Button btnYes = view.findViewById(R.id.btn_yes);
        Button btnNo = view.findViewById(R.id.btn_no);
        Button viewScheduleButton = view.findViewById(R.id.view_schedule_button);

        // 캘린더 날짜 선택 이벤트
        calendarView.setOnDateChangeListener((view1, year, month, dayOfMonth) -> {
            selectedDate = year + "-" + (month + 1) + "-" + dayOfMonth; // 날짜 저장
        });

        // "등록" 버튼 클릭 이벤트
        registerButton.setOnClickListener(v -> {
            if (selectedDate == null || inputSchedule.getText().toString().trim().isEmpty()) {
                Toast.makeText(getContext(), "날짜와 일정을 입력해주세요.", Toast.LENGTH_SHORT).show();
            } else {
                // "등록" 버튼 숨기기, "예"와 "아니오" 버튼 보이기
                registerButton.setVisibility(View.INVISIBLE);
                confirmationButtons.setVisibility(View.VISIBLE);
            }
        });

        // "예" 버튼 클릭 이벤트
        btnYes.setOnClickListener(v -> {
            String scheduleText = inputSchedule.getText().toString().trim();
            scheduleList.add(new Schedule(selectedDate, scheduleText));
            Toast.makeText(getContext(), "일정이 등록되었습니다.", Toast.LENGTH_SHORT).show();

            // 버튼 상태 초기화
            inputSchedule.setText(""); // 입력 필드 초기화
            registerButton.setVisibility(View.VISIBLE);
            confirmationButtons.setVisibility(View.GONE);
        });

        // "아니오" 버튼 클릭 이벤트
        btnNo.setOnClickListener(v -> {
            inputSchedule.setText(""); // 입력 필드 초기화
            Toast.makeText(getContext(), "등록이 취소되었습니다.", Toast.LENGTH_SHORT).show();

            // 버튼 상태 초기화
            registerButton.setVisibility(View.VISIBLE);
            confirmationButtons.setVisibility(View.GONE);
        });

        // "일정 보기" 버튼 클릭 이벤트
        viewScheduleButton.setOnClickListener(v -> {
            Bundle bundle = new Bundle();
            bundle.putSerializable("schedule_list", (Serializable) scheduleList);

            ScheduleListFragment scheduleListFragment = new ScheduleListFragment();
            scheduleListFragment.setArguments(bundle);

            getParentFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, scheduleListFragment)
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }
}
