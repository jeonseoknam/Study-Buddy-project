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

import com.example.studybuddy.R;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MyTimerFragment extends Fragment {




    private TextView timerText;         // 시간 표시 텍스트
    private ProgressBar circularProgress; // 원형 ProgressBar
    private Button deleteButton, pauseButton, registerButton;

    private Handler handler = new Handler(Looper.getMainLooper());
    private int elapsedTime = 0; // 경과 시간 (단위: 초)
    private boolean isRunning = false;

    private TimerService timerService;
    private boolean isServiceBound = false;


    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            TimerService.TimerBinder binder = (TimerService.TimerBinder) service;
            timerService = binder.getService();
            isServiceBound = true;
            updateUI(); // 서비스에 연결된 후 UI 업데이트
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

    public static MyTimerFragment newInstance(String param1, String param2) {
        MyTimerFragment fragment = new MyTimerFragment();
        Bundle args = new Bundle();
        args.putString("param1", param1);
        args.putString("param2", param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @NonNull ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_my_timer, container, false);

        // View 초기화
        timerText = view.findViewById(R.id.timer_text);
        circularProgress = view.findViewById(R.id.circular_progress);
        deleteButton = view.findViewById(R.id.delete_button);
        pauseButton = view.findViewById(R.id.pause_button);
        registerButton = view.findViewById(R.id.register_button);

        // 초기 타이머 설정
        updateTimer();

        // 버튼 동작 설정
        pauseButton.setOnClickListener(v -> toggleTimer());
        deleteButton.setOnClickListener(v -> resetTimer());

        if (isServiceBound) {
            timerService.resetTimer();
            updateUI();
        }

        //등록 버튼
        registerButton.setOnClickListener(v -> {
            String time = timerText.getText().toString();
            saveTimeToPreferences(time);
            Toast.makeText(getContext(), "시간이 등록되었습니다!", Toast.LENGTH_SHORT).show();


            // TimeListFragment로 이동
            requireActivity().getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, new TimeListFragment())
                    .addToBackStack(null)
                    .commit();
        });


        return view;
    }

    private void updateTimer() {
        // 경과 시간을 분, 초로 변환
        int minutes = elapsedTime / 60;
        int seconds = elapsedTime % 60;
        String time = String.format("%02d:%02d", minutes, seconds);
        timerText.setText(time);

        // ProgressBar 업데이트 (60분이 기준)
        int progress = (int) (((double) elapsedTime % 3600) / 3600 * 100); // 3600초 = 60분
        circularProgress.setProgress(progress);
    }


    private void resetTimer() {
        // 타이머 리셋
        isRunning = false;
        handler.removeCallbacks(timerRunnable);
        elapsedTime = 0;
        updateTimer();
        pauseButton.setText("시작");
    }

    @Override
    public void onStart() {
        super.onStart();
        // TimerService 바인딩
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


    private void toggleTimer() {
        if (isServiceBound) {
            if (timerService.getElapsedTime() > 0 && pauseButton.getText().equals("일시정지")) {
                timerService.stopTimer();
                pauseButton.setText("시작");
            } else {
                timerService.startTimer();
                pauseButton.setText("일시정지");
            }
            updateUI();
        }
    }

    private void updateUI() {
        if (isServiceBound) {
            int elapsedTime = timerService.getElapsedTime();
            int minutes = elapsedTime / 60;
            int seconds = elapsedTime % 60;
            String time = String.format("%02d:%02d", minutes, seconds);
            timerText.setText(time);

            int progress = (elapsedTime % 3600) * 100 / 3600;
            circularProgress.setProgress(progress);
        }
    }

    private void saveTimeToPreferences(String time) {
        SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("TimeData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();

        // 기존 시간 목록 불러오기
        Gson gson = new Gson();
        String json = sharedPreferences.getString("time_list", null);
        List<String> timeList = new ArrayList<>();
        if (json != null) {
            Type type = new TypeToken<List<String>>() {}.getType();
            timeList = gson.fromJson(json, type);
        }

        // 새 시간 추가
        timeList.add(time);

        // 다시 저장
        String updatedJson = gson.toJson(timeList);
        editor.putString("time_list", updatedJson);
        editor.apply();
    }

}
