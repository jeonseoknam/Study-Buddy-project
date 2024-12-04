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
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GoalBoardFragment extends Fragment {

    private static final String TAG = "GoalBoardFragment";
    private static final String PREFS_NAME = "com.example.studybuddy.GoalPrefs";
    private static final String GOAL_LIST_KEY = "goal_list";

    private TextView goalBoardTitle;

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
        String chatRoomId = getArguments().getString("chatRoomId");
        goalBoardTitle = view.findViewById(R.id.chatRoomNameText);
        goalBoardTitle.setText(chatRoomId);

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
            // 현재 chatRoomId를 가져온다


            // Intent에 chatRoomId를 담아서 Activity로 전달
            Intent intent = new Intent(getActivity(), GoalRegistrationActivity.class);
            intent.putExtra("chatRoomId", chatRoomId); // chatRoomId 전달
            startActivityForResult(intent, 1001);
        });

        // Firebase에서 목표 불러오기
        refreshGoals();
        loadGoalsFromFirebase();

        ImageButton backButton = view.findViewById(R.id.backButton);

        backButton.setOnClickListener(v -> {

            if (getParentFragmentManager().getBackStackEntryCount() > 0) {
                getParentFragmentManager().popBackStack();
            } else {
                requireActivity().onBackPressed();
            }
        });

        return view;
    }





    @Override
    public void onResume() {
        super.onResume();
        loadGoalsFromFirebase(); // Firebase에서 목표 리스트를 다시 불러옵니다.
        refreshGoals();
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

    public void refreshGoals() {
        Log.d(TAG, "Refreshing goals with current filter: " + currentFilter);
        loadGoalsFromFirebase(); // Firestore에서 목표 리스트를 다시 불러옵니다.
    }

    // 목표 상세 정보 화면으로 이동하는 메소드
    private void openGoalDetailsFragment(String goalId) {
        if (getActivity() != null && goalId != null) {
            String chatRoomId = getArguments().getString("chatRoomId"); // 현재 Fragment의 chatRoomId 가져오기

            if (chatRoomId == null || chatRoomId.isEmpty()) {
                Log.e("logchk", "ChatRoomId is missing!");
                Toast.makeText(getContext(), "ChatRoomId가 유효하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            Bundle bundle = new Bundle();
            bundle.putString("goalId", goalId);
            bundle.putString("chatRoomId", chatRoomId); // chatRoomId 전달

            GoalDetailsFragment goalDetailsFragment = new GoalDetailsFragment();
            goalDetailsFragment.setArguments(bundle);

            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.fragment_container, goalDetailsFragment);
            transaction.addToBackStack(null);
            transaction.commit();
        }
    }

    private void loadGoalsFromFirebase() {
        String chatRoomId = getArguments().getString("chatRoomId"); // 전달받은 chatRoomId
        CollectionReference goalsRef = db.collection("Goals").document(chatRoomId).collection("goals");
        CollectionReference userInfoRef = db.collection("userInfo"); // userInfo 컬렉션 참조
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();

        // 닉네임 캐시를 위한 맵
        Map<String, String> userNicknameMap = new HashMap<>();

        // userInfo 컬렉션에서 닉네임 또는 이름 미리 로드
        userInfoRef.get().addOnSuccessListener(userSnapshot -> {
            for (QueryDocumentSnapshot userDoc : userSnapshot) {
                String userId = userDoc.getId();
                String displayName = userDoc.getString("Nickname") != null
                        ? userDoc.getString("Nickname") // 닉네임 우선
                        : userDoc.getString("Name") != null
                        ? userDoc.getString("Name") // 실명 대체
                        : "Unknown User";

                userNicknameMap.put(userId, displayName);
            }

            // Goals 데이터를 로드
            goalsRef.get().addOnSuccessListener(goalSnapshot -> {
                originalGoalList.clear();

                for (QueryDocumentSnapshot doc : goalSnapshot) {
                    String id = doc.getId();
                    String title = doc.getString("title");
                    int dueInDays = doc.getLong("dueInDays") != null ? doc.getLong("dueInDays").intValue() : 0;
                    int likes = doc.getLong("likes") != null ? doc.getLong("likes").intValue() : 0;
                    int goalLikes = doc.getLong("goalLikes") != null ? doc.getLong("goalLikes").intValue() : 0;
                    String userId = doc.getString("userId");
                    String status = doc.getString("status") != null ? doc.getString("status") : "pending";
                    String certificationImageUrl = doc.getString("certificationImageUrl");
                    String certificationDescription = doc.getString("certificationDescription");

                    // 닉네임 또는 실명 가져오기
                    String displayName = userNicknameMap.getOrDefault(userId, "Unknown User");

                    // Goal 객체 생성
                    Goal goal = new Goal(
                            id,
                            title,
                            dueInDays,
                            likes,
                            goalLikes,
                            status,
                            certificationImageUrl,
                            certificationDescription,
                            displayName
                    );

                    // 탭에 따른 목표 필터링 (나의 목표 / 스터디 메이트의 목표)
                    if ((currentTab.equals("나의 목표") && currentUserId.equals(userId)) ||
                            (currentTab.equals("스터디 메이트의 목표") && !currentUserId.equals(userId))) {
                        originalGoalList.add(goal);
                    }
                }

                // 모든 데이터를 정렬 및 필터링 후 RecyclerView 갱신
                sortGoalsByDueDate();
                filterGoals(currentFilter);
            }).addOnFailureListener(e -> Log.e(TAG, "Failed to fetch goals", e));
        }).addOnFailureListener(e -> Log.e(TAG, "Failed to fetch user info", e));
    }


//    private void loadGoalsFromFirebase() {
//        String chatRoomId = getArguments().getString("chatRoomId"); // 전달받은 chatRoomId
//        CollectionReference goalsRef = db.collection("Goals").document(chatRoomId).collection("goals");
//        CollectionReference userInfoRef = db.collection("userInfo"); // userInfo 컬렉션 참조
//        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        // 닉네임 캐시를 위한 맵
//        Map<String, String> userNicknameMap = new HashMap<>();
//
//        // userInfo 컬렉션에서 닉네임 미리 로드
//        userInfoRef.get().addOnSuccessListener(userSnapshot -> {
//            for (QueryDocumentSnapshot userDoc : userSnapshot) {
//                String userId = userDoc.getId();
//                String nickName = userDoc.getString("Nickname");
//                userNicknameMap.put(userId, nickName != null ? nickName : "Unknown User");
//            }
//
//            // Goals 데이터를 로드
//            goalsRef.get().addOnSuccessListener(goalSnapshot -> {
//                originalGoalList.clear();
//
//                for (QueryDocumentSnapshot doc : goalSnapshot) {
//                    String id = doc.getId();
//                    String title = doc.getString("title");
//                    int dueInDays = doc.getLong("dueInDays") != null ? doc.getLong("dueInDays").intValue() : 0;
//                    int likes = doc.getLong("likes") != null ? doc.getLong("likes").intValue() : 0;
//                    int goalLikes = doc.getLong("goalLikes") != null ? doc.getLong("goalLikes").intValue() : 0;
//                    String userId = doc.getString("userId");
//                    String status = doc.getString("status") != null ? doc.getString("status") : "pending";
//                    String certificationImageUrl = doc.getString("certificationImageUrl");
//                    String certificationDescription = doc.getString("certificationDescription");
//
//                    // 닉네임 캐시에서 가져오기
//                    String nickName = userNicknameMap.getOrDefault(userId, "Unknown User");
//
//                    // Goal 객체 생성
//                    Goal goal = new Goal(
//                            id,
//                            title,
//                            dueInDays,
//                            likes,
//                            goalLikes,
//                            status,
//                            certificationImageUrl,
//                            certificationDescription,
//                            nickName
//                    );
//
//                    // 탭에 따른 목표 필터링 (나의 목표 / 스터디 메이트의 목표)
//                    if ((currentTab.equals("나의 목표") && currentUserId.equals(userId)) ||
//                            (currentTab.equals("스터디 메이트의 목표") && !currentUserId.equals(userId))) {
//                        originalGoalList.add(goal);
//                    }
//                }
//
//                // 모든 데이터를 정렬 및 필터링 후 RecyclerView 갱신
//                sortGoalsByDueDate();
//                filterGoals(currentFilter);
//            }).addOnFailureListener(e -> Log.e(TAG, "Failed to fetch goals", e));
//        }).addOnFailureListener(e -> Log.e(TAG, "Failed to fetch user info", e));
//    }

//    private void loadGoalsFromFirebase() {
//        String chatRoomId = getArguments().getString("chatRoomId"); // 전달받은 chatRoomId
//        CollectionReference goalsRef = db.collection("Goals").document(chatRoomId).collection("goals");
//        CollectionReference userInfoRef = db.collection("userInfo"); // userInfo 컬렉션 참조
//        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid();
//
//        // 익명 여부를 저장할 변수
//        final boolean[] isAnonymous = new boolean[1];
//
//        // 닉네임/실명 캐시를 위한 맵
//        Map<String, String> userNicknameMap = new HashMap<>();
//
//        // 익명 여부 가져오기
//        db.collection("chatSettings").document(chatRoomId)
//                .get()
//                .addOnSuccessListener(chatSettingDoc -> {
//                    if (chatSettingDoc.exists() && chatSettingDoc.contains("isAnonymous")) {
//                        isAnonymous[0] = chatSettingDoc.getBoolean("isAnonymous"); // 익명 여부 저장
//
//                        // userInfo 컬렉션에서 닉네임 또는 실명 미리 로드
//                        userInfoRef.get().addOnSuccessListener(userSnapshot -> {
//                            for (QueryDocumentSnapshot userDoc : userSnapshot) {
//                                String userId = userDoc.getId();
//                                String valueToStore;
//
//                                // 익명 여부에 따라 닉네임 또는 실명 선택
//                                if (isAnonymous[0]) {
//                                    valueToStore = userDoc.getString("Nickname");
//                                    valueToStore = valueToStore != null ? valueToStore : "Unknown User";
//                                } else {
//                                    valueToStore = userDoc.getString("Name");
//                                    valueToStore = valueToStore != null ? valueToStore : "Unknown User";
//                                }
//
//                                // 맵에 저장
//                                userNicknameMap.put(userId, valueToStore);
//                            }
//
//                            // Goals 데이터를 로드
//                            goalsRef.get().addOnSuccessListener(goalSnapshot -> {
//                                originalGoalList.clear();
//
//                                for (QueryDocumentSnapshot doc : goalSnapshot) {
//                                    String id = doc.getId();
//                                    String title = doc.getString("title");
//                                    int dueInDays = doc.getLong("dueInDays") != null ? doc.getLong("dueInDays").intValue() : 0;
//                                    int likes = doc.getLong("likes") != null ? doc.getLong("likes").intValue() : 0;
//                                    int goalLikes = doc.getLong("goalLikes") != null ? doc.getLong("goalLikes").intValue() : 0;
//                                    String userId = doc.getString("userId");
//                                    String status = doc.getString("status") != null ? doc.getString("status") : "pending";
//                                    String certificationImageUrl = doc.getString("certificationImageUrl");
//                                    String certificationDescription = doc.getString("certificationDescription");
//
//                                    // 닉네임 또는 실명 가져오기
//                                    String userDisplayName = userNicknameMap.getOrDefault(userId, "Unknown User");
//
//                                    // Goal 객체 생성
//                                    Goal goal = new Goal(
//                                            id,
//                                            title,
//                                            dueInDays,
//                                            likes,
//                                            goalLikes,
//                                            status,
//                                            certificationImageUrl,
//                                            certificationDescription,
//                                            userDisplayName
//                                    );
//
//                                    // 탭에 따른 목표 필터링 (나의 목표 / 스터디 메이트의 목표)
//                                    if ((currentTab.equals("나의 목표") && currentUserId.equals(userId)) ||
//                                            (currentTab.equals("스터디 메이트의 목표") && !currentUserId.equals(userId))) {
//                                        originalGoalList.add(goal);
//                                    }
//                                }
//
//                                // 모든 데이터를 정렬 및 필터링 후 RecyclerView 갱신
//                                sortGoalsByDueDate();
//                                filterGoals(currentFilter);
//                            }).addOnFailureListener(e -> Log.e(TAG, "Failed to fetch goals", e));
//                        }).addOnFailureListener(e -> Log.e(TAG, "Failed to fetch user info", e));
//                    }
//                }).addOnFailureListener(e -> Log.e(TAG, "Failed to fetch chat settings", e));
//    }



    private void filterGoals(String filter) {
        List<Goal> filteredGoals = new ArrayList<>();

        for (Goal goal : originalGoalList) {
            if (goal == null || goal.getStatus() == null) continue;

            int dueInDays = goal.getDueInDays();
            if (filter.equals("설정한 목표(미인증)") && goal.getStatus().equals("pending") && dueInDays >= 0) {
                filteredGoals.add(goal);
            } else if (filter.equals("인증한 목표") && goal.getStatus().equals("certified")) {
                filteredGoals.add(goal);
            } else if (filter.equals("인정된 목표") && goal.getStatus().equals("approved")) {
                filteredGoals.add(goal);
            } else if (filter.equals("미인정된 목표") &&
                    (goal.getStatus().equals("unapproved") || (goal.getStatus().equals("pending") && dueInDays < 0))) {
                filteredGoals.add(goal);
            }
        }

        Log.d(TAG, "Filtered Goals: " + filteredGoals.size() + " for filter: " + filter);
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

    private void sortGoalsByDueDate() {
        Collections.sort(originalGoalList, (goal1, goal2) -> {
            return Integer.compare(goal1.getDueInDays(), goal2.getDueInDays());
        });

        // 정렬된 리스트를 어댑터에 반영
        goalAdapter.updateGoals(new ArrayList<>(originalGoalList));
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
    private String nickname;

    public Goal(String id, String title, int dueInDays, int likes, int goalLikes, String status, String certificationImageUrl, String certificationDescription, String nickname) {
        this.id = id;
        this.title = title;
        this.dueInDays = dueInDays;
        this.likes = likes;
        this.goalLikes = goalLikes;
        this.status = status; // 초기화
        this.certificationImageUrl = certificationImageUrl;
        this.certificationDescription = certificationDescription;
        this.nickname = nickname;
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

    public String getNickname() {return nickname; }


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
        holder.likeCountTextView.setText(String.valueOf(goal.getGoalLikes()));

        String nickname = goal.getNickname();
        holder.userName.setText(nickname != null ? nickname : "Unknown User");

        String imageUrl = goal.getCertificationImageUrl();
        if (imageUrl != null && !imageUrl.isEmpty()) {
            Glide.with(holder.thumbnail.getContext())
                    .load(imageUrl)
                    .placeholder(R.drawable.placeholder_image)
                    .error(R.drawable.placeholder_image)
                    .into(holder.thumbnail);
        } else {
            holder.thumbnail.setImageResource(R.drawable.placeholder_image);
        }

        holder.goalTitleTextView.setVisibility(View.VISIBLE);
        holder.dueTextView.setVisibility(View.VISIBLE);
        holder.likeCountTextView.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return goalList.size();
    }

    public void updateGoals(List<Goal> updatedGoals) {
        this.goalList.clear();
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

        TextView userName;

        ImageView thumbnail;

        public GoalViewHolder(@NonNull View itemView, final OnItemClickListener listener) {
            super(itemView);
            goalTitleTextView = itemView.findViewById(R.id.goalTitleTextView);
            dueTextView = itemView.findViewById(R.id.dueTextView);
            likeCountTextView = itemView.findViewById(R.id.likeCountTextView);
            thumbnail = itemView.findViewById(R.id.thumbnail);
            userName = itemView.findViewById(R.id.userIdTextView);

            itemView.setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION && listener != null) { // 유효한 포지션인지 확인
                    listener.onItemClick(position);
                }
            });
        }
    }

}