package com.example.studybuddy;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;

import com.example.studybuddy.GoalRegistrationActivity;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class GoalBoardFragment extends Fragment {

    private static final String TAG = "GoalBoardFragment";
    private static final String PREFS_NAME = "com.example.studybuddy.GoalPrefs";
    private static final String GOAL_LIST_KEY = "goal_list";

    private RecyclerView recyclerViewGoals;
    private GoalAdapter goalAdapter;
    private List<Goal> originalGoalList; // 원본 데이터를 저장할 리스트
    private Spinner spinnerGoalFilter;
    private FloatingActionButton addGoalButton;
    private SharedPreferences sharedPreferences;

    private FirebaseFirestore db;

    private String currentFilter = "설정한 목표(미인증)";
    private String currentTab = "나의 목표"; // 기본 탭 설정

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goal_board, container, false);

        // SharedPreferences 설정
        sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Firebase Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // 목표 리스트 초기화
        originalGoalList = new ArrayList<>();

        // RecyclerView 설정
        recyclerViewGoals = view.findViewById(R.id.recyclerViewGoals);
        recyclerViewGoals.setLayoutManager(new LinearLayoutManager(getContext()));
        goalAdapter = new GoalAdapter(new ArrayList<>());
        recyclerViewGoals.setAdapter(goalAdapter);

        // RecyclerView 아이템 클릭 리스너 설정
        goalAdapter.setOnItemClickListener(new GoalAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (position >= 0 && position < goalAdapter.getGoalList().size()) {
                    String goalId = goalAdapter.getGoalList().get(position).getId();
                    openGoalDetailsFragment(goalId);
                }
            }
        });

        // TabLayout 설정
        TabLayout tabLayout = view.findViewById(R.id.goalTab);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getText().toString(); // 선택된 탭 저장
                loadGoalsFromFirebase(); // 탭 변경 시 Firebase에서 데이터 다시 로드
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {}

            @Override
            public void onTabReselected(TabLayout.Tab tab) {}
        });

        // Spinner 설정
        spinnerGoalFilter = view.findViewById(R.id.goalSetSpinner);
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, getResources().getStringArray(R.array.goal_filter_options));
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerGoalFilter.setAdapter(spinnerAdapter);

        // Spinner 아이템 선택 이벤트 처리
        spinnerGoalFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                currentFilter = (String) parent.getItemAtPosition(position); // 선택된 필터 저장
                filterGoals(currentFilter); // 필터링 적용
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });

        // FloatingActionButton 설정 (목표 추가)
        addGoalButton = view.findViewById(R.id.addGoalButton);
        addGoalButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), GoalRegistrationActivity.class);
            startActivityForResult(intent, 1001);
        });

        // Firebase에서 목표 불러오기
        loadGoalsFromFirebase();

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadGoalsFromFirebase(); // Firebase에서 목표 리스트를 다시 불러옵니다.
    }

    @Override
    public void onPause() {
        super.onPause();
        saveGoalsToPreferences();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001 && resultCode == Activity.RESULT_OK) {
            loadGoalsFromFirebase(); // 목표 데이터 다시 불러오기
        }
    }

    // 목표 상세 정보 화면으로 이동하는 메소드
    private void openGoalDetailsFragment(String goalId) {
        if (getActivity() != null && goalId != null) {
            Bundle bundle = new Bundle();
            bundle.putString("goalId", goalId);
            GoalDetailsFragment goalDetailsFragment = new GoalDetailsFragment();
            goalDetailsFragment.setArguments(bundle);

            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, goalDetailsFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    // Firebase에서 목표 리스트를 불러오는 메소드
    private void loadGoalsFromFirebase() {
        CollectionReference goalsRef = db.collection("Goals");
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        goalsRef.addSnapshotListener((value, error) -> {
            if (error != null) {
                Log.e(TAG, "Listen failed: " + error);
                return;
            }

            if (value != null) {
                originalGoalList.clear();

                for (QueryDocumentSnapshot doc : value) {
                    String id = doc.getId();
                    String title = doc.getString("title");
                    int dueInDays = doc.getLong("dueInDays") != null ? doc.getLong("dueInDays").intValue() : 0;
                    int likes = doc.getLong("likes") != null ? doc.getLong("likes").intValue() : 0;
                    int goalLikes = doc.getLong("goalLikes") != null ? doc.getLong("goalLikes").intValue() : 0;
                    String status = doc.getString("status");
                    String userId = doc.getString("userId");

                    if (status == null) status = "pending";

                    String certificationImageUrl = doc.getString("certificationImageUrl");
                    String certificationDescription = doc.getString("certificationDescription");

                    Goal goal = new Goal(id, title, dueInDays, likes, goalLikes, status, certificationImageUrl, certificationDescription);

                    // 탭에 따른 목표 필터링 (나의 목표 / 스터디 메이트의 목표)
                    if ((currentTab.equals("나의 목표") && currentUserId.equals(userId)) ||
                            (currentTab.equals("스터디 메이트의 목표") && !currentUserId.equals(userId))) {
                        originalGoalList.add(goal);
                    }
                }

                // 필터 적용
                filterGoals(currentFilter);
            }
        });
    }

    // 필터링 로직을 적용하는 메소드
    private void filterGoals(String filter) {
        List<Goal> filteredGoals = new ArrayList<>();

        for (Goal goal : originalGoalList) {
            if (goal == null || goal.getStatus() == null) continue;

            int dueInDays = goal.getDueInDays();
            if (filter.equals("설정한 목표(미인증)") && goal.getStatus().equals("pending") && dueInDays > 0) {
                filteredGoals.add(goal);
            } else if (filter.equals("인증한 목표") && goal.getStatus().equals("certified")) {
                filteredGoals.add(goal);
            } else if (filter.equals("인정된 목표") && goal.getStatus().equals("approved")) {
                filteredGoals.add(goal);
            } else if (filter.equals("미인정된 목표") &&
                    (goal.getStatus().equals("unapproved") || (goal.getStatus().equals("pending") && dueInDays <= 0))) {
                filteredGoals.add(goal);
            }
        }

        goalAdapter.updateGoals(filteredGoals);
    }

    // 목표 리스트를 SharedPreferences에 저장하는 메소드
    private void saveGoalsToPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        JSONArray jsonArray = new JSONArray();
        for (Goal goal : originalGoalList) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", goal.getId());
                jsonObject.put("title", goal.getTitle());
                jsonObject.put("dueInDays", goal.getDueInDays());
                jsonObject.put("likes", goal.getLikes());
                jsonObject.put("goalLikes", goal.getGoalLikes());
                jsonObject.put("status", goal.getStatus());
                jsonObject.put("certificationImageUrl", goal.getCertificationImageUrl());
                jsonObject.put("certificationDescription", goal.getCertificationDescription());

                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                Log.e(TAG, "Failed to serialize goal: " + e.getMessage());
            }
        }
        editor.putString(GOAL_LIST_KEY, jsonArray.toString());
        editor.apply();
    }
}

class Goal {
    private String id;
    private String title;
    private int dueInDays;
    private int likes;
    private int goalLikes;
    private String status; // 추가된 상태 필드
    private String certificationImageUrl;
    private String certificationDescription;

    public Goal(String id, String title, int dueInDays, int likes, int goalLikes, String status, String certificationImageUrl, String certificationDescription) {
        this.id = id;
        this.title = title;
        this.dueInDays = dueInDays;
        this.likes = likes;
        this.goalLikes = goalLikes;
        this.status = status; // 초기화
        this.certificationImageUrl = certificationImageUrl;
        this.certificationDescription = certificationDescription;
    }

    // Getter/Setter 메서드 추가
    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
    // Getter 메소드 추가
    public String getCertificationImageUrl() {
        return certificationImageUrl;
    }

    public String getCertificationDescription() {
        return certificationDescription;
    }
    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public int getDueInDays() {
        return dueInDays;
    }

    public String getDueDate() {
        return "D-Day " + dueInDays;
    }

    public int getLikes() {
        return likes;
    }

    public int getGoalLikes() {
        return goalLikes;
    }



}

class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {
    private List<Goal> goalList;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public GoalAdapter(List<Goal> goalList) {
        this.goalList = goalList;
    }

    @NonNull
    @Override
    public GoalViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_goal, parent, false);
        return new GoalViewHolder(view, listener);
    }

    @Override
    public void onBindViewHolder(@NonNull GoalViewHolder holder, int position) {
        Goal goal = goalList.get(position);
        holder.goalTitleTextView.setText(goal.getTitle());
        holder.dueTextView.setText(goal.getDueDate());
        holder.likeCountTextView.setText(goal.getLikes() + "/" + goal.getGoalLikes());
    }

    @Override
    public int getItemCount() {
        return goalList.size();
    }

    public void updateGoals(List<Goal> updatedGoals) {
        this.goalList = updatedGoals;
        notifyDataSetChanged(); // RecyclerView 갱신
    }

    // 여기서 goalList를 반환하는 메서드를 추가
    public List<Goal> getGoalList() {
        return goalList;
    }

    static class GoalViewHolder extends RecyclerView.ViewHolder {
        TextView goalTitleTextView;
        TextView dueTextView;
        TextView likeCountTextView;

        public GoalViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            goalTitleTextView = itemView.findViewById(R.id.goalTitleTextView);
            dueTextView = itemView.findViewById(R.id.dueTextView);
            likeCountTextView = itemView.findViewById(R.id.likeCountTextView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) { // 유효한 포지션인지 확인
                    listener.onItemClick(position);
                }
            });
        }
    }

}