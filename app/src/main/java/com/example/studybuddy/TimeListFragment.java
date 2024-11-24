package com.example.studybuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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
import java.util.List;

public class TimeListFragment extends Fragment {

    private RecyclerView recyclerView;
    private TimeListAdapter adapter;
    private List<StudySession> timeList; // StudySession 객체 리스트
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_list, container, false);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        timeList = new ArrayList<>();

        // Firestore에서 데이터 읽어오기
        fetchStudySessions();

        return view;
    }

    private void fetchStudySessions() {
        String userId = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : "anonymous";

        CollectionReference studySessionsRef = firestore.collection("study_sessions");
        studySessionsRef
                .whereEqualTo("user_id", userId) // 사용자 ID 필터
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    timeList.clear();
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        StudySession session = document.toObject(StudySession.class);
                        session.setId(document.getId()); // Firestore 문서 ID 저장
                        timeList.add(session);
                    }

                    // RecyclerView Adapter 초기화
                    adapter = new TimeListAdapter(timeList, firestore);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "데이터를 불러오지 못했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
