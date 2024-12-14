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
    private String savedChatName, chatNameSet, nameField;

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
        fetchSettingAndRankingData();

        return view;
    }

    private void fetchSettingAndRankingData() {
        if (savedChatName == null || savedChatName.isEmpty()) {
            Toast.makeText(getContext(), "유효하지 않은 채팅방 ID입니다.", Toast.LENGTH_SHORT).show();
            return;
        }

        // 설정 데이터 가져오기
        firestore.collection("soongsil")
                .document("chat")
                .collection("chatRoom")
                .document(chatNameSet)
                .collection(savedChatName)
                .document("chatSetting")
                .collection("setting")
                .document("setting")
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        // 익명/실명 설정 확인
                        String nameSetting = documentSnapshot.getString("name");
                        nameField = "anonymous".equals(nameSetting) ? "Nickname" : "Name";

                        // 랭킹 데이터 가져오기
                        fetchRankingData();
                    } else {
                        Toast.makeText(getContext(), "채팅방 설정 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show();
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "설정 데이터를 가져오지 못했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void fetchRankingData() {
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

                    // 총합 데이터를 설정에 따라 변환
                    convertUserIdToName(userTotalTimes);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "랭킹 데이터를 가져오지 못했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void convertUserIdToName(Map<String, Long> userTotalTimes) {
        firestore.collection("userInfo")
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, String> userIdToName = new HashMap<>();

                    // 유저 ID와 닉네임/실명 매칭
                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        String userId = document.getString("UID");
                        String name = document.getString(nameField); // 설정에 따른 필드 사용
                        if (userId != null && name != null) {
                            userIdToName.put(userId, name);
                        }
                    }

                    // 이름과 총 시간을 리스트로 변환
                    rankingList.clear();
                    for (Map.Entry<String, Long> entry : userTotalTimes.entrySet()) {
                        String name = userIdToName.get(entry.getKey());
                        if (name != null) {
                            rankingList.add(new ChatTimerRankingItem(name, entry.getValue()));
                        } else {
                            rankingList.add(new ChatTimerRankingItem(entry.getKey(), entry.getValue())); // 이름이 없으면 ID 표시
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

