package com.example.studybuddy;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class ChatTimerFragment extends Fragment {
    private FirebaseFirestore firestore;
    private FirebaseAuth firebaseAuth;


    private TextView timerText;         // 시간 표시 텍스트
    private ProgressBar circularProgress; // 원형 ProgressBar
    private Button deleteButton, pauseButton, registerButton, rankingButton, historyButton;
    private String chatID;
    private String nickName, chatOpen, chatCode = "";
    private SharedPreferences userPref;

    private Handler handler = new Handler(Looper.getMainLooper());
    private int elapsedTime = 0; // 경과 시간 (단위: 초)
    private boolean isRunning = false;

    private TimerService timerService;
    private boolean isServiceBound = false;
    TextView tv_chat_timer_title;

    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.TimerBinder binder = (TimerService.TimerBinder) service;
            timerService = binder.getService();
            isServiceBound = true;

            // 서비스와 상태 동기화
            elapsedTime = timerService.getElapsedTime();
            isRunning = timerService.isRunning();
            updateTimer();
            updatePauseButton();

            // 기존 Runnable 제거 후 실행
            handler.removeCallbacks(timerRunnable);

            if (isRunning) {
                handler.post(timerRunnable);
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isServiceBound = false;
        }
    };

    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                elapsedTime++; // 1초마다 증가
                updateTimer();
                handler.postDelayed(this, 1000); // 1초마다 반복 실행
            }
        }
    };

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_timer, container, false);

        // Firestore와 FirebaseAuth 초기화
        firestore = FirebaseFirestore.getInstance();
        firebaseAuth = FirebaseAuth.getInstance();
        // View 초기화
        timerText = view.findViewById(R.id.timer_text);
        circularProgress = view.findViewById(R.id.circular_progress);
        deleteButton = view.findViewById(R.id.delete_button);
        pauseButton = view.findViewById(R.id.pause_button);
        registerButton = view.findViewById(R.id.register_button);
        rankingButton = view.findViewById(R.id.ranking_button);
        historyButton = view.findViewById(R.id.history_button);
        tv_chat_timer_title = view.findViewById(R.id.tv_chat_timer_title);

        // 초기 타이머 설정
        updateTimer();

        // 버튼 동작 설정
        pauseButton.setOnClickListener(v -> toggleTimer());
        deleteButton.setOnClickListener(v -> resetTimer());

        String chatRoomId = getArguments().getString("chatRoomId");
        tv_chat_timer_title.setText(chatRoomId+"\n타이머");


        registerButton.setOnClickListener(v -> {

            RegisterBottomSheet bottomSheet = new RegisterBottomSheet();
            bottomSheet.setRegisterListener((subjectName, memo) -> {
                // Firestore에 데이터 저장
                saveTimeToFirestore(timerText.getText().toString(), subjectName, memo);
            });
            bottomSheet.show(getParentFragmentManager(), bottomSheet.getTag());
            Toast.makeText(getContext(), "공부 시간을 저장합니다.", Toast.LENGTH_SHORT).show();
        });

        rankingButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "랭킹 페이지로 이동합니다.", Toast.LENGTH_SHORT).show();

            ChatTimerRankingFragment fragment = new ChatTimerRankingFragment();
            Bundle args = new Bundle();
            args.putString("chatRoomId", chatRoomId);
            args.putString("nickname", nickName);
            fragment.setArguments(args);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new ChatTimerRankingFragment())
                    .addToBackStack(null)
                    .commit();
        });

        historyButton.setOnClickListener(v -> {
            Toast.makeText(getContext(), "나의 공부 기록 페이지로 이동합니다",Toast.LENGTH_SHORT).show();

            TimeListFragment fragment = new TimeListFragment();
            Bundle args = new Bundle();
            args.putString("chatRoomId", chatRoomId);
            args.putString("nickname", nickName);
            fragment.setArguments(args);

            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new TimeListFragment())
                    .addToBackStack(null)
                    .commit();
        });

        return view;
    }

    private void saveTimeToFirestore(String elapsedTime, String subjectName, String memo) {
        // 현재 사용자 ID 가져오기 (익명 사용자 처리)
        String userId = firebaseAuth.getCurrentUser() != null ? firebaseAuth.getCurrentUser().getUid() : "anonymous";

        // Firestore에 저장할 데이터 준비
        Map<String, Object> studySession = new HashMap<>();
        studySession.put("user_id", userId);
        studySession.put("elapsed_time", elapsedTime);
        studySession.put("subject_name", subjectName);
        studySession.put("memo", memo);
        studySession.put("timestamp", System.currentTimeMillis());

        // Firestore에 데이터 추가
        firestore.collection("study_sessions")
                .add(studySession)
                .addOnSuccessListener(documentReference -> {
                    Toast.makeText(getContext(), "공부 기록이 저장되었습니다!", Toast.LENGTH_SHORT).show();
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "저장 실패: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                });
    }

    private void updateTimer() {
        // 경과 시간을 분, 초로 변환
        int minutes = elapsedTime / 60;
        int seconds = elapsedTime % 60;
        String time = String.format("%02d:%02d", minutes, seconds);
        timerText.setText(time);

        // ProgressBar 업데이트 (60분 기준)
        int progress = (int) (((double) elapsedTime % 3600) / 3600 * 100);
        circularProgress.setProgress(progress);
    }

    private void resetTimer() {
        if (isServiceBound) {
            timerService.resetTimer();
            elapsedTime = 0;
            isRunning = false;
            handler.removeCallbacks(timerRunnable);
            updateTimer();
            pauseButton.setText("시작");
        }
    }

    private void toggleTimer() {
        if (isServiceBound) {
            handler.removeCallbacks(timerRunnable);

            if (isRunning) {
                timerService.stopTimer();
                isRunning = false;
            } else {
                timerService.startTimer();
                isRunning = true;
                handler.post(timerRunnable);
            }

            updateTimer();
            updatePauseButton();
        }
    }

    private void updatePauseButton() {
        if (isRunning) {
            pauseButton.setText("일시정지");
        } else {
            pauseButton.setText("시작");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        Intent intent = new Intent(getActivity(), TimerService.class);
        requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isServiceBound) {
            requireActivity().unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isServiceBound && timerService != null) {
            elapsedTime = timerService.getElapsedTime();
            isRunning = timerService.isRunning();
            updateTimer();
            updatePauseButton();
            handler.removeCallbacks(timerRunnable);

            if (isRunning) {
                handler.post(timerRunnable);
            }
        }
    }
}
