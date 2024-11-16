package com.example.studybuddy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.studybuddy.R;
import com.foo.login_register.TimeListAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class TimeListFragment extends Fragment {

    private RecyclerView recyclerView;
    private TimeListAdapter adapter;
    private List<String> timeList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // SharedPreferences에서 시간 목록 불러오기
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("TimeData", Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPreferences.getString("time_list", null);
        if (json != null) {
            Type type = new TypeToken<List<String>>() {}.getType();
            timeList = gson.fromJson(json, type);
        } else {
            timeList = new ArrayList<>();
        }

        adapter = new TimeListAdapter(timeList);
        recyclerView.setAdapter(adapter);

        return view;
    }
}
