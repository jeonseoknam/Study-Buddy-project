package com.example.studybuddy;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatTimeRankingFragment extends Fragment {

    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private RankingAdapter adapter;
    private List<RankingItem> rankingList = new ArrayList<>();
    private String chatRoomId;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_time_ranking, container, false);

        // Firebase 초기화
        firestore = FirebaseFirestore.getInstance();

        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.ranking_recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 채팅방 ID 가져오기 (Bundle에서 전달받음)
        chatRoomId = getArguments().getString("chatRoomId", "");

        if (!chatRoomId.isEmpty()) {
            fetchChatRoomRankingData(chatRoomId); // 특정 채팅방의 랭킹 데이터 가져오기
        } else {
            Toast.makeText(getContext(), "채팅방 ID를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show();
        }

        return view;
    }

    private void fetchChatRoomRankingData(String chatRoomId) {
        firestore.collection("study_sessions")
                .whereEqualTo("chatRoomId", chatRoomId)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Long> userTotalTimes = new HashMap<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String userId = document.getString("user_id");
                        String elapsedTime = document.getString("elapsed_time");

                        String[] timeParts = elapsedTime.split(":");
                        long sessionTimeInSeconds = Integer.parseInt(timeParts[0]) * 60
                                + Integer.parseInt(timeParts[1]);

                        userTotalTimes.put(userId, userTotalTimes.getOrDefault(userId, 0L) + sessionTimeInSeconds);
                    }

                    // Map을 List로 변환 및 정렬
                    rankingList.clear();
                    for (Map.Entry<String, Long> entry : userTotalTimes.entrySet()) {
                        rankingList.add(new RankingItem(entry.getKey(), entry.getValue()));
                    }
                    rankingList.sort((o1, o2) -> Long.compare(o2.getTotalTime(), o1.getTotalTime()));

                    // RecyclerView Adapter에 데이터 설정
                    adapter = new RankingAdapter(rankingList);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "랭킹 데이터를 가져오지 못했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
