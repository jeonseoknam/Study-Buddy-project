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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class GoalBoardFragment extends Fragment {

    private static final String TAG = "GoalBoardFragment";
    private static final String PREFS_NAME = "com.example.studybuddy.GoalPrefs";
    private static final String GOAL_LIST_KEY = "goal_list";
    private RecyclerView recyclerViewGoals;
    private GoalAdapter goalAdapter;
    private List<Goal> goalList;
    private Spinner spinnerGoalFilter;
    private FloatingActionButton addGoalButton;
    private SharedPreferences sharedPreferences;
    private FirebaseFirestore db;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_goal_board, container, false);

        // SharedPreferences 설정
        sharedPreferences = getContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Firebase Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // 목표 리스트 초기화
        goalList = loadGoalsFromPreferences();
        Log.d(TAG, "Loaded goal list size: " + goalList.size());

        // RecyclerView 설정
        recyclerViewGoals = view.findViewById(R.id.recyclerViewGoals);
        if (recyclerViewGoals == null) {
            Log.e(TAG, "RecyclerView is null. Please check the XML layout.");
        } else {
            Log.d(TAG, "RecyclerView found successfully.");
        }
        recyclerViewGoals.setLayoutManager(new LinearLayoutManager(getContext()));

        // 어댑터 설정
        goalAdapter = new GoalAdapter(goalList);
        recyclerViewGoals.setAdapter(goalAdapter);
        Log.d(TAG, "Adapter set to RecyclerView. Goal list size: " + goalList.size());

        // RecyclerView에서 아이템 클릭 시 GoalDetailsFragment 열기
        goalAdapter.setOnItemClickListener(new GoalAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                String goalId = goalList.get(position).getId(); // 목표의 고유 ID 가져오기
                openGoalDetailsFragment(goalId);
            }
        });

        // Firebase에서 목표 불러오기
        loadGoalsFromFirebase();

        // Spinner 설정
        spinnerGoalFilter = view.findViewById(R.id.goalSetSpinner);
        if (spinnerGoalFilter == null) {
            Log.e(TAG, "Spinner is null. Please check the XML layout.");
        } else {
            Log.d(TAG, "Spinner found successfully.");
        }
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), R.layout.spinner_item, getResources().getStringArray(R.array.goal_filter_options));
        spinnerAdapter.setDropDownViewResource(R.layout.spinner_item);
        spinnerGoalFilter.setAdapter(spinnerAdapter);

        // Spinner 아이템 선택 이벤트 처리
        spinnerGoalFilter.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedItem = (String) parent.getItemAtPosition(position);
                Log.d(TAG, "Spinner item selected: " + selectedItem);
                filterGoals(selectedItem);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Log.d(TAG, "No spinner item selected.");
            }
        });

        // FloatingActionButton 설정 (목표 추가)
        addGoalButton = view.findViewById(R.id.addGoalButton);
        if (addGoalButton == null) {
            Log.e(TAG, "FloatingActionButton is null. Please check the XML layout.");
        } else {
            Log.d(TAG, "FloatingActionButton found successfully.");
        }
        addGoalButton.setOnClickListener(v -> {
            Intent intent = new Intent(getActivity(), GoalRegistrationActivity.class);
            startActivityForResult(intent, 1001);
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        // Firebase에서 목표 불러오기 (화면이 다시 활성화될 때마다 최신 데이터를 반영)
        loadGoalsFromFirebase();
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


    // 목표 필터링 메소드
    private void filterGoals(String filter) {
        Log.d(TAG, "Filtering goals with filter: " + filter);
        // 필터에 따라 goalList 업데이트 및 어댑터에 반영
        List<Goal> filteredGoals = new ArrayList<>();
        for (Goal goal : goalList) {
            if (filter.equals("인증한 목표") && goal.isAchieved() && !goal.isRecognized()) {
                filteredGoals.add(goal);
            } else if (filter.equals("미인증 목표") && !goal.isAchieved()) {
                filteredGoals.add(goal);
            } else if (filter.equals("설정한 목표(미인증)")) {
                filteredGoals.add(goal);
            } else if (filter.equals("인정된 목표") && goal.isRecognized()) {
                filteredGoals.add(goal);
            }
        }

        // D-Day 정렬 적용
        Collections.sort(filteredGoals, new Comparator<Goal>() {
            @Override
            public int compare(Goal g1, Goal g2) {
                return Integer.compare(g1.getDueInDays(), g2.getDueInDays());
            }
        });

        Log.d(TAG, "Filtered goal list size: " + filteredGoals.size());
        goalAdapter.updateGoals(filteredGoals);
    }

    private void openGoalDetailsFragment(String goalId) {
        Bundle bundle = new Bundle();
        bundle.putString("goalId", goalId);
        GoalDetailsFragment goalDetailsFragment = new GoalDetailsFragment();
        goalDetailsFragment.setArguments(bundle);

        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.fragment_container, goalDetailsFragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    // Firebase에서 목표 리스트를 불러오는 메소드
    // Firebase에서 목표 리스트를 불러오는 메소드
    private void loadGoalsFromFirebase() {
        CollectionReference goalsRef = db.collection("Goals");
        goalsRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                if (error != null) {
                    Log.e(TAG, "Listen failed: " + error);
                    return;
                }

                if (value != null) {
                    goalList.clear();
                    for (QueryDocumentSnapshot doc : value) {
                        String id = doc.getId(); // Firebase에서 고유 ID 가져오기
                        String title = doc.getString("title");
                        int dueInDays = doc.getLong("dueInDays") != null ? doc.getLong("dueInDays").intValue() : 0;
                        int likes = doc.getLong("likes") != null ? doc.getLong("likes").intValue() : 0;
                        int goalLikes = doc.getLong("goalLikes") != null ? doc.getLong("goalLikes").intValue() : 0;
                        boolean isAchieved = doc.getBoolean("isAchieved") != null && doc.getBoolean("isAchieved");
                        boolean isRecognized = doc.getBoolean("isRecognized") != null && doc.getBoolean("isRecognized");

                        // 새로운 필드 추가: 인증 이미지 URL과 설명
                        String certificationImageUrl = doc.getString("certificationImageUrl");
                        String certificationDescription = doc.getString("certificationDescription");

                        // Goal 객체 생성 시 새로운 필드 포함
                        goalList.add(new Goal(id, title, dueInDays, likes, goalLikes, isAchieved, isRecognized, certificationImageUrl, certificationDescription));
                    }

                    // D-Day 기준 정렬 적용
                    Collections.sort(goalList, new Comparator<Goal>() {
                        @Override
                        public int compare(Goal g1, Goal g2) {
                            return Integer.compare(g1.getDueInDays(), g2.getDueInDays());
                        }
                    });

                    goalAdapter.updateGoals(goalList);
                    Log.d(TAG, "Goals loaded from Firebase. Size: " + goalList.size());
                }
            }
        });
    }

    // 목표 리스트를 SharedPreferences에 저장하는 메소드
    private void saveGoalsToPreferences() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        JSONArray jsonArray = new JSONArray();
        for (Goal goal : goalList) {
            try {
                JSONObject jsonObject = new JSONObject();
                jsonObject.put("id", goal.getId());
                jsonObject.put("title", goal.getTitle());
                jsonObject.put("dueInDays", goal.getDueInDays());
                jsonObject.put("likes", goal.getLikes());
                jsonObject.put("goalLikes", goal.getGoalLikes());
                jsonObject.put("isAchieved", goal.isAchieved());
                jsonObject.put("isRecognized", goal.isRecognized());
                // 새로운 필드 추가: 인증 이미지 URL과 설명
                jsonObject.put("certificationImageUrl", goal.getCertificationImageUrl());
                jsonObject.put("certificationDescription", goal.getCertificationDescription());

                jsonArray.put(jsonObject);
            } catch (JSONException e) {
                Log.e(TAG, "Failed to serialize goal: " + e.getMessage());
            }
        }
        editor.putString(GOAL_LIST_KEY, jsonArray.toString());
        editor.apply();
        Log.d(TAG, "Goals saved to SharedPreferences. Size: " + goalList.size());
    }

    // SharedPreferences에서 목표 리스트를 불러오는 메소드
    private List<Goal> loadGoalsFromPreferences() {
        List<Goal> loadedGoals = new ArrayList<>();
        String jsonString = sharedPreferences.getString(GOAL_LIST_KEY, "");
        if (!jsonString.isEmpty()) {
            try {
                JSONArray jsonArray = new JSONArray(jsonString);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonObject = jsonArray.getJSONObject(i);
                    String id = jsonObject.getString("id");
                    String title = jsonObject.getString("title");
                    int dueInDays = jsonObject.getInt("dueInDays");
                    int likes = jsonObject.getInt("likes");
                    int goalLikes = jsonObject.getInt("goalLikes");
                    boolean isAchieved = jsonObject.getBoolean("isAchieved");
                    boolean isRecognized = jsonObject.getBoolean("isRecognized");
                    // 새로운 필드 추가: 인증 이미지 URL과 설명
                    String certificationImageUrl = jsonObject.optString("certificationImageUrl", null);
                    String certificationDescription = jsonObject.optString("certificationDescription", null);

                    // Goal 객체 생성 시 새로운 필드 포함
                    loadedGoals.add(new Goal(id, title, dueInDays, likes, goalLikes, isAchieved, isRecognized, certificationImageUrl, certificationDescription));
                }
            } catch (JSONException e) {
                Log.e(TAG, "Failed to load goals from SharedPreferences: " + e.getMessage());
            }
        }
        return loadedGoals;
    }

}

class Goal {
    private String id;
    private String title;
    private int dueInDays;
    private int likes;
    private int goalLikes;
    private boolean isAchieved;
    private boolean isRecognized;
    private String certificationImageUrl; // 추가된 필드
    private String certificationDescription; // 추가된 필드

    public Goal(String id, String title, int dueInDays, int likes, int goalLikes, boolean isAchieved, boolean isRecognized, String certificationImageUrl, String certificationDescription) {
        this.id = id;
        this.title = title;
        this.dueInDays = dueInDays;
        this.likes = likes;
        this.goalLikes = goalLikes;
        this.isAchieved = isAchieved;
        this.isRecognized = isRecognized;
        this.certificationImageUrl = certificationImageUrl;
        this.certificationDescription = certificationDescription;
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

    public boolean isAchieved() {
        return isAchieved;
    }

    public boolean isRecognized() {
        return isRecognized;
    }

    public void setAchieved(boolean achieved) {
        isAchieved = achieved;
    }

    public void setRecognized(boolean recognized) {
        isRecognized = recognized;
    }
}

class GoalAdapter extends RecyclerView.Adapter<GoalAdapter.GoalViewHolder> {
    private List<Goal> goalList;
    private static final String TAG = "GoalAdapter";
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
        notifyDataSetChanged();
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

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (listener != null) {
                        int position = getAdapterPosition();
                        if (position != RecyclerView.NO_POSITION) {
                            listener.onItemClick(position);
                        }
                    }
                }
            });
        }
    }
}
