package com.example.studybuddy;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;

import java.util.ArrayList;
import java.util.List;

public class MyNotificationsFragment extends Fragment {

    private static final String TAG = "MyNotificationsFragment";

    private RecyclerView recyclerView;
    private List<String> notificationList;
    private NotificationAdapter notificationAdapter;
    private SharedPreferences userPref;

    private FirebaseFirestore db; // Firestore 객체 선언
    private ListenerRegistration notificationListener; // 리스너 등록을 관리할 변수

    public MyNotificationsFragment() {
        // Required empty public constructor
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_notifications, container, false);

        // Firestore 초기화
        db = FirebaseFirestore.getInstance();

        // RecyclerView 초기화
        recyclerView = view.findViewById(R.id.notificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 알림 데이터와 어댑터 초기화
        notificationList = new ArrayList<>();
        notificationAdapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(notificationAdapter);

        TextView schoolNameView = view.findViewById(R.id.schoolChatNameText);

        userPref = getContext().getSharedPreferences("userData", Context.MODE_PRIVATE);
        String schoolName = userPref.getString("School","none");

        if (schoolName.equals("soongsil")){
            schoolNameView.setText("숭실대학교 채팅");
        } else if (schoolName.equals("seoul")) {
            schoolNameView.setText("서울대학교 채팅");
        }
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 현재 사용자 ID 가져오기

        // Firestore 리스너 등록
        notificationListener = db.collection("notifications")
                .whereEqualTo("userId", currentUserId) // 사용자 필터
                .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING) // 최신순 정렬
                .addSnapshotListener((queryDocumentSnapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Error listening to notifications: " + e.getMessage());
                        return;
                    }

                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
                        notificationList.clear(); // 기존 데이터 초기화
                        for (DocumentSnapshot document : queryDocumentSnapshots) {
                            String chatName = document.getString("chatRoomId");
                            String title = document.getString("title");
                            String message = document.getString("message");

                            // 알림 데이터 추가
                            notificationList.add("채팅방 \"" + chatName + "\"\n" + message);
                        }
                        // RecyclerView 갱신
                        notificationAdapter.notifyDataSetChanged();
                    }
                });
    }


//    @Override
//    public void onStart() {
//        super.onStart();
//        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 현재 사용자 ID 가져오기
//
//        // Firestore 리스너 등록
//        notificationListener = db.collection("notifications")
//                .whereEqualTo("userId", currentUserId)
//                .addSnapshotListener((queryDocumentSnapshots, e) -> {
//                    if (e != null) {
//                        Log.e(TAG, "Error listening to notifications: " + e.getMessage());
//                        return;
//                    }
//
//                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
//                        notificationList.clear(); // 기존 데이터 초기화
//                        for (DocumentSnapshot document : queryDocumentSnapshots) {
//                            String chatName = document.getString("chatRoomId");
//                            String title = document.getString("title");
//                            String message = document.getString("message");
//
//                            // 알림 데이터 추가
//                            notificationList.add("채팅방 \""+chatName +"\"\n" + "" + message);
//                        }
//                        // RecyclerView 갱신
//                        notificationAdapter.notifyDataSetChanged();
//                    }
//                });
//    }


    @Override
    public void onStop() {
        super.onStop();
        // Firestore 리스너 제거
        if (notificationListener != null) {
            notificationListener.remove();
        }
    }

    public void addNotification(String newNotification) {
        notificationList.add(newNotification);
        notificationAdapter.notifyDataSetChanged();
    }
}
