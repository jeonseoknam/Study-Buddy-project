package com.example.studybuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link MyProfileFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MyProfileFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;
    private SharedPreferences userPref;
    private FirebaseAuth mAuth;

    public MyProfileFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment MyProfileFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static MyProfileFragment newInstance(String param1, String param2) {
        MyProfileFragment fragment = new MyProfileFragment();
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
        View view = inflater.inflate(R.layout.fragment_my_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
//공부 시간 통계 페이지 프래그먼트를 띄운다
        Button studytimeButton = view.findViewById(R.id.btn_studyTime);
        studytimeButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, MyStudyTimeFragment.newInstance(mParam1,mParam2))
                    .addToBackStack(null)
                    .commit();
        });

        //프로필 프래그먼트에서 캘린더 프래그먼트를 띄웠다. 프래그먼트에서 프래그먼트를 띄우려면
        //부모 액티비티의 프래그먼트 매니저를 이용한다
        Button calendarButton = view.findViewById(R.id.btn_personalCalender);
        calendarButton.setOnClickListener(v -> {
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, MyCalendarFragment.newInstance())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userPref = getContext().getSharedPreferences("userData", Context.MODE_PRIVATE);

        ImageView profile = view.findViewById(R.id.profile_image);
        if (userPref.getString("Profile",null) != null){
            Glide.with(this).load(userPref.getString("Profile",null)).into(profile);
        }
        TextView nameWithNicknameText = view.findViewById(R.id.user_nameText);
        nameWithNicknameText.setText(userPref.getString("Name","none") + "("+userPref.getString("Nickname", "none")+")");
        TextView majorText = view.findViewById(R.id.major_infoText);
        majorText.setText(userPref.getString("Major","none"));

        Button settingButton = view.findViewById(R.id.btn_setting);
        settingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), SettingActivity.class);
                startActivity(intent);
            }
        });

        Button logoutbutton = view.findViewById(R.id.btn_logout);
        logoutbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                getActivity().finish();
            }
        });
    }
}