package com.example.studybuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.ArrayList;
import java.util.List;

public class ScheduleListFragment extends Fragment {
    private List<Schedule> scheduleList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_schedule_list, container, false);

        // 데이터 받기
        if (getArguments() != null) {
            scheduleList = (List<Schedule>) getArguments().getSerializable("schedule_list");
        } else {
            scheduleList = new ArrayList<>();
        }

        // 리스트뷰 설정
        ListView scheduleListView = view.findViewById(R.id.schedule_list_view);
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                getContext(),
                android.R.layout.simple_list_item_1,
                convertScheduleToString(scheduleList)
        );
        scheduleListView.setAdapter(adapter);

        return view;
    }

    private List<String> convertScheduleToString(List<Schedule> schedules) {
        List<String> scheduleStrings = new ArrayList<>();
        for (Schedule schedule : schedules) {
            scheduleStrings.add(schedule.getDate() + ": " + schedule.getDescription());
        }
        return scheduleStrings;
    }
}
