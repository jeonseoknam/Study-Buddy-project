package com.example.studybuddy;

import android.content.Context;
import android.content.SharedPreferences;
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

import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatTimerRankingFragment extends Fragment {

    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private ChatTimerRankingAdapter adapter;
    private List<ChatTimerRankingItem> rankingList;
    private String chatRoomId;
    String savedChatName, chatNameSet;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_timer_ranking, container, false);

        // Firestore 초기화
        firestore = FirebaseFirestore.getInstance();

        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 데이터 리스트 초기화
        rankingList = new ArrayList<>();

        // chatRoomId 전달받기
        if (getArguments() != null) {
            chatRoomId = getArguments().getString("chatRoomId", "defaultChatRoom");
        }

        // SharedPreferences 사용
        SharedPreferences chatIdPref = getContext().getSharedPreferences("chatName", Context.MODE_PRIVATE);
        savedChatName = chatIdPref.getString("Name", "defaultChatName");
        chatNameSet = chatIdPref.getString("open", "none");

        // Firestore 데이터 가져오기
        fetchRankingData();

        return view;
    }

    private void fetchRankingData() {
        if (savedChatName == null || savedChatName.isEmpty()) {
            Toast.makeText(getContext(), "유효하지 않은 채팅방 ID입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        firestore.collection("soongsil")
                .document("chat")
                .collection("chatRoom")
                .document(chatNameSet)
                .collection(savedChatName)
                .document("timerRecords")
                .collection("records")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Long> userTotalTimes = new HashMap<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        try {
                            String userId = document.getString("user_id");
                            String elapsedTime = document.getString("elapsed_time");

                            // 시간 데이터 파싱
                            String[] timeParts = elapsedTime.split(":");
                            long sessionTimeInSeconds = Integer.parseInt(timeParts[0]) * 60
                                    + Integer.parseInt(timeParts[1]);

                            // 유저별 총 시간 합산
                            userTotalTimes.put(userId, userTotalTimes.getOrDefault(userId, 0L) + sessionTimeInSeconds);
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    }

                    // 총합 데이터를 닉네임으로 변환
                    convertUserIdToNickname(userTotalTimes);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "데이터를 가져오지 못했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void convertUserIdToNickname(Map<String, Long> userTotalTimes) {
        firestore.collection("userInfo")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, String> userIdToNickname = new HashMap<>();

                    // 유저 ID와 닉네임 매칭
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String userId = document.getString("UID");
                        String nickname = document.getString("Nickname");
                        if (userId != null && nickname != null) {
                            userIdToNickname.put(userId, nickname);
                        }
                    }

                    // 닉네임과 총 시간을 리스트로 변환
                    rankingList.clear();
                    for (Map.Entry<String, Long> entry : userTotalTimes.entrySet()) {
                        String nickname = userIdToNickname.get(entry.getKey());
                        if (nickname != null) {
                            rankingList.add(new ChatTimerRankingItem(nickname, entry.getValue()));
                        } else {
                            rankingList.add(new ChatTimerRankingItem(entry.getKey(), entry.getValue())); // 닉네임이 없으면 ID 표시
                        }
                    }

                    // 데이터 정렬
                    rankingList.sort((o1, o2) -> Long.compare(o2.getTotalTime(), o1.getTotalTime()));

                    // RecyclerView Adapter 설정
                    adapter = new ChatTimerRankingAdapter(rankingList);
                    recyclerView.setAdapter(adapter);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "유저 정보를 가져오지 못했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }
}
