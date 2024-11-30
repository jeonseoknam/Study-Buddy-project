package com.example.studybuddy;

import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.List;

public class ScheduleListActivity extends AppCompatActivity {
    private FirebaseFirestore db;
    private RecyclerView recyclerView;
    private ScheduleAdapter adapter;
    private List<ScheduleModel> scheduleList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule_list);

        db = FirebaseFirestore.getInstance();
        recyclerView = findViewById(R.id.schedule_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        scheduleList = new ArrayList<>();
        adapter = new ScheduleAdapter(scheduleList);
        recyclerView.setAdapter(adapter);

        String chatRoomId = getIntent().getStringExtra("chatRoomId");
        fetchSchedulesFromFirestore(chatRoomId);
    }

    private void fetchSchedulesFromFirestore(String chatRoomId) {
        CollectionReference schedulesRef = db.collection("chatRoom")
                .document("singleChat")
                .collection(chatRoomId)
                .document("schedules")
                .collection("schedules");

        schedulesRef.get().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                scheduleList.clear();
                for (QueryDocumentSnapshot document : task.getResult()) {
                    String title = document.getString("title");
                    String date = document.getString("date");
                    String time = document.getString("time");
                    String name = document.getString("name");
                    String profileUrl = document.getString("profileUrl");

                    ScheduleModel schedule = new ScheduleModel(title, date, time, name, profileUrl);
                    scheduleList.add(schedule);
                }
                adapter.notifyDataSetChanged();
            } else {
                Toast.makeText(this, "일정을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
