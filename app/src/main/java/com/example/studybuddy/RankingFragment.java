package com.example.studybuddy;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

public class RankingFragment extends Fragment {

    private FirebaseFirestore firestore;
    private RecyclerView recyclerView;
    private RankingAdapter adapter;
    private List<RankingItem> rankingList = new ArrayList<>();
    private Spinner sortSpinner;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_ranking, container, false);

        // Firebase 초기화
        firestore = FirebaseFirestore.getInstance();

        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 빈 리스트로 초기 Adapter 설정
        adapter = new RankingAdapter(new ArrayList<>());
        recyclerView.setAdapter(adapter);

        // Spinner 초기화
        sortSpinner = view.findViewById(R.id.sort_spinner);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(getContext(),
                R.array.sort_options, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sortSpinner.setAdapter(adapter);

        // Spinner 선택 이벤트 처리
        sortSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedOption = parent.getItemAtPosition(position).toString();
                fetchRankingData(selectedOption); // 정렬 기준에 따라 데이터 가져오기
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 선택되지 않은 경우 처리
            }
        });

        return view;
    }

    private void fetchRankingData(String sortOption) {
        long currentTime = System.currentTimeMillis();
        long startTime = getStartTimeForSortOption(sortOption, currentTime);

        firestore.collection("study_sessions")
                .whereGreaterThanOrEqualTo("timestamp", startTime) // 선택한 기간 데이터만 가져오기
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    Map<String, Long> userTotalTimes = new HashMap<>();

                    // 기존 리스트 초기화
                    rankingList.clear();

                    // Firestore의 모든 문서 순회
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
                            Log.e("RankingFragment", "Error processing document ID: " + document.getId(), e);
                        }
                    }

                    // 중복 제거 후 닉네임 매칭
                    convertUserIdToNickname(userTotalTimes);
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "랭킹 데이터를 가져오지 못했습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }


    private long getStartTimeForSortOption(String sortOption, long currentTime) {
        long oneDayInMillis = 24 * 60 * 60 * 1000;
        long oneWeekInMillis = 7 * oneDayInMillis;
        long oneMonthInMillis = 30 * oneDayInMillis;

        switch (sortOption) {
            case "하루":
                return currentTime - oneDayInMillis;
            case "1주":
                return currentTime - oneWeekInMillis;
            case "1달":
                return currentTime - oneMonthInMillis;
            default:
                return 0; // 모든 데이터 포함
        }
    }


    private void convertUserIdToNickname(Map<String, Long> userTotalTimes) {
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

        // Firestore에서 userId -> Nickname 매핑
        for (String userId : userTotalTimes.keySet()) {
            Task<DocumentSnapshot> task = firestore.collection("userInfo")
                    .document(userId)
                    .get()
                    .addOnSuccessListener(documentSnapshot -> {
                        String nickname = documentSnapshot.getString("Nickname");
                        if (nickname != null) {
                            rankingList.add(new RankingItem(nickname, userTotalTimes.get(userId)));
                        }
                    });
            tasks.add(task);
        }

        // 모든 작업 완료 후 RecyclerView 업데이트
        Tasks.whenAllComplete(tasks).addOnCompleteListener(task -> {
            rankingList.sort((o1, o2) -> Long.compare(o2.getTotalTime(), o1.getTotalTime()));
            adapter = new RankingAdapter(rankingList);
            recyclerView.setAdapter(adapter);
        });
    }




    private boolean isWithinTimeRange(String sortOption, long timestamp, long currentTime) {
        long oneDayInMillis = 24 * 60 * 60 * 1000;
        long oneWeekInMillis = 7 * oneDayInMillis;
        long oneMonthInMillis = 30 * oneDayInMillis;

        switch (sortOption) {
            case "하루":
                return currentTime - timestamp <= oneDayInMillis;
            case "1주":
                return currentTime - timestamp <= oneWeekInMillis;
            case "1달":
                return currentTime - timestamp <= oneMonthInMillis;
            default:
                return true;
        }
    }
}
