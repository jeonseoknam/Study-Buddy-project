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

    private void startTimerRunnable() {
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                if (isRunning) {
                    elapsedTime++;
                    updateTimer();
                    handler.postDelayed(this, 1000); // 1초마다 실행
                }
            }
        }, 1000);
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


    @Override
    public void onStart() {
        super.onStart();
         //TimerService 바인딩
        Intent intent = new Intent(getActivity(), TimerService.class);
        requireActivity().bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (isServiceBound) {
            saveTimerState();
            requireActivity().unbindService(serviceConnection);
            isServiceBound = false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        if (isServiceBound && timerService != null) { // TimerService와 연결된 상태인지 확인
            // TimerService에서 상태를 가져와 동기화
            elapsedTime = timerService.getElapsedTime();
            isRunning = timerService.isRunning();

            // UI 업데이트
            updateTimer();
            updatePauseButton(); // 버튼 텍스트를 업데이트

            // 기존 Runnable 제거 후 실행
            handler.removeCallbacks(timerRunnable);

            // 타이머가 실행 중이라면 Runnable로 지속 업데이트
            if (isRunning) {
                handler.post(timerRunnable);
            }
        }
    }
    private void updatePauseButton() {
        if (isRunning) {
            pauseButton.setText("일시정지");
        } else {
            pauseButton.setText("시작");
        }
    }

    private void toggleTimer() {
        if (isServiceBound) {
            handler.removeCallbacks(timerRunnable); // 기존 Runnable 제거

            if (isRunning) {
                timerService.stopTimer();
                isRunning = false;
            } else {
                timerService.startTimer();
                isRunning = true;
                handler.post(timerRunnable); // 새로운 Runnable 추가
            }

            updateTimer();
            updatePauseButton(); // 버튼 텍스트 업데이트
        }
    }


    private void saveTimerState() {
        if (isServiceBound && timerService != null) {
            // SharedPreferences 초기화
            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences("TimerState", Context.MODE_PRIVATE);
            SharedPreferences.Editor editor = sharedPreferences.edit();

            // 타이머 상태 저장
            editor.putInt("elapsed_time", timerService.getElapsedTime()); // 경과 시간 저장
            editor.putBoolean("is_running", isRunning); // 타이머 실행 상태 저장

            // SharedPreferences에 적용
            editor.apply();
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
