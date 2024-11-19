package com.example.studybuddy;

import android.app.Notification;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyNotificationsFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyNotificationsFragment extends Fragment {

    private RecyclerView recyclerView;
    private List<String> notificationList;
    private NotificationAdapter notificationAdapter;

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public MyNotificationsFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyNotificationsFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyNotificationsFragment newInstance(String param1, String param2) {
        MyNotificationsFragment fragment = new MyNotificationsFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_my_notifications, container, false);

        recyclerView = view.findViewById(R.id.notificationRecyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 예제 알림 데이터
        notificationList = new ArrayList<>();
        notificationList.add("톰 크루즈 님이 목표를 등록했습니다.");
        notificationList.add("5분 이상 핸드폰을 사용했습니다!");
        notificationList.add("빌리 아일.. 님이 목표를 인정해주었습니다. 2/3");
        notificationList.add("알고리즘 기말고사가 15일 남았습니다.");

        notificationAdapter = new NotificationAdapter(notificationList);
        recyclerView.setAdapter(notificationAdapter);

        return view;


        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_my_notifications, container, false);
    }

    public void addNotification(String newNotification) {
        notificationList.add(newNotification);
        notificationAdapter.notifyDataSetChanged();
    }
}