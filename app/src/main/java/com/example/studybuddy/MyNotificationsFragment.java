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
                .whereEqualTo("userId", currentUserId)
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
                            notificationList.add("채팅방 \""+chatName +"\"\n" + "" + message);
                        }
                        // RecyclerView 갱신
                        notificationAdapter.notifyDataSetChanged();
                    }
                });
    }


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

//package com.example.studybuddy;
//
//import android.app.Notification;
//import android.os.Bundle;
//
//import androidx.fragment.app.Fragment;
//import androidx.recyclerview.widget.LinearLayoutManager;
//import androidx.recyclerview.widget.RecyclerView;
//
//import android.util.Log;
//import android.view.LayoutInflater;
//import android.view.View;
//import android.view.ViewGroup;
//
//import com.google.firebase.auth.FirebaseAuth;
//import com.google.firebase.firestore.DocumentSnapshot;
//
//import java.util.ArrayList;
//import java.util.List;
//
///**
// * A simple {@link Fragment} subclass.
// * Use the {@link MyNotificationsFragment#newInstance} factory method to
// * create an instance of this fragment.
// */
//public class MyNotificationsFragment extends Fragment {
//
//    private RecyclerView recyclerView;
//    private List<String> notificationList;
//    private NotificationAdapter notificationAdapter;
//
//    // TODO: Rename parameter arguments, choose names that match
//    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
//    private static final String ARG_PARAM1 = "param1";
//    private static final String ARG_PARAM2 = "param2";
//
//    // TODO: Rename and change types of parameters
//    private String mParam1;
//    private String mParam2;
//
//    public MyNotificationsFragment() {
//        // Required empty public constructor
//    }
//
//    /**
//     * Use this factory method to create a new instance of
//     * this fragment using the provided parameters.
//     *
//     * @param param1 Parameter 1.
//     * @param param2 Parameter 2.
//     * @return A new instance of fragment MyNotificationsFragment.
//     */
//    // TODO: Rename and change types and number of parameters
//    public static MyNotificationsFragment newInstance(String param1, String param2) {
//        MyNotificationsFragment fragment = new MyNotificationsFragment();
//        Bundle args = new Bundle();
//        args.putString(ARG_PARAM1, param1);
//        args.putString(ARG_PARAM2, param2);
//        fragment.setArguments(args);
//        return fragment;
//    }
//
//    @Override
//    public void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        if (getArguments() != null) {
//            mParam1 = getArguments().getString(ARG_PARAM1);
//            mParam2 = getArguments().getString(ARG_PARAM2);
//        }
//    }
//
//    @Override
//    public View onCreateView(LayoutInflater inflater, ViewGroup container,
//                             Bundle savedInstanceState) {
//        View view = inflater.inflate(R.layout.fragment_my_notifications, container, false);
//
//        // RecyclerView 초기화
//        recyclerView = view.findViewById(R.id.notificationRecyclerView);
//        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
//
//        // 알림 데이터를 저장할 리스트와 어댑터 초기화
//        notificationList = new ArrayList<>();
//        notificationAdapter = new NotificationAdapter(notificationList);
//        recyclerView.setAdapter(notificationAdapter);
//
//        // Firestore에서 실시간 알림 데이터 가져오기
//        String currentUserId = FirebaseAuth.getInstance().getCurrentUser().getUid(); // 현재 사용자 ID
//        db.collection("notifications")
//                .whereEqualTo("userId", currentUserId) // 현재 사용자 ID의 알림만 가져오기
//                .addSnapshotListener((queryDocumentSnapshots, e) -> {
//                    if (e != null) {
//                        Log.e("logchk", "Error listening to notifications: " + e.getMessage());
//                        return;
//                    }
//
//                    if (queryDocumentSnapshots != null && !queryDocumentSnapshots.isEmpty()) {
//                        notificationList.clear(); // 기존 리스트 초기화
//                        for (DocumentSnapshot document : queryDocumentSnapshots) {
//                            String title = document.getString("title");
//                            String message = document.getString("message");
//
//                            // 알림 데이터 추가
//                            notificationList.add(title + ": " + message);
//                        }
//                        // RecyclerView 업데이트
//                        notificationAdapter.notifyDataSetChanged();
//                    }
//                });
//
//        return view;
//    }
//
//
////    @Override
////    public View onCreateView(LayoutInflater inflater, ViewGroup container,
////                             Bundle savedInstanceState) {
////
////        View view = inflater.inflate(R.layout.fragment_my_notifications, container, false);
////
////        recyclerView = view.findViewById(R.id.notificationRecyclerView);
////        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
////
////        // 예제 알림 데이터
////        notificationList = new ArrayList<>();
////        notificationList.add("톰 크루즈 님이 목표를 등록했습니다.");
////        notificationList.add("5분 이상 핸드폰을 사용했습니다!");
////        notificationList.add("빌리 아일.. 님이 목표를 인정해주었습니다. 2/3");
////        notificationList.add("알고리즘 기말고사가 15일 남았습니다.");
////
////        notificationAdapter = new NotificationAdapter(notificationList);
////        recyclerView.setAdapter(notificationAdapter);
////
////        return view;
////
////
////        // Inflate the layout for this fragment
////        //return inflater.inflate(R.layout.fragment_my_notifications, container, false);
////    }
//
//    public void addNotification(String newNotification) {
//        notificationList.add(newNotification);
//        notificationAdapter.notifyDataSetChanged();
//    }
//}