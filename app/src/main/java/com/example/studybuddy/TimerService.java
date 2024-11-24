package com.example.studybuddy;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

public class TimerService extends Service {

    private final IBinder binder = new TimerBinder();
    private Handler handler = new Handler();
    private int elapsedTime = 0; // 타이머 시간 (초)
    private boolean isRunning = false;


    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            if (isRunning) {
                elapsedTime++;
                handler.postDelayed(this, 1000); // 1초마다 증가
            }
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.O)
    @Override
    public void onCreate() {
        super.onCreate();
        startForegroundService(); // 포그라운드 서비스 시작
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public boolean isRunning() {
        return isRunning;
    }

    public class TimerBinder extends Binder {
        public TimerService getService() {
            return TimerService.this;
        }
    }

    public void startTimer() {
        if (!isRunning) {
            isRunning = true;
            handler.post(timerRunnable);
        }
    }

    public void stopTimer() {
        isRunning = false;
        handler.removeCallbacks(timerRunnable);
    }

    public void resetTimer() {
        isRunning = false;
        handler.removeCallbacks(timerRunnable);
        elapsedTime = 0;
    }

    public int getElapsedTime() {
        return elapsedTime;
    }

    @SuppressLint({"MissingPermission", "ForegroundServiceType"})
    private void startForegroundService() {
        String channelId = "timer_channel";
        String channelName = "타이머 서비스";

        // API 26 이상에서 NotificationChannel 생성
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel(
                    channelId,
                    "Timer Service",
                    NotificationManager.IMPORTANCE_LOW
            );

            channel.setDescription("타이머가 백그라운드에서 실행 중입니다.");
            NotificationManager manager = getSystemService(NotificationManager.class);
            if (manager != null) {
                manager.createNotificationChannel(channel);
            }
        }

        Notification notification = new NotificationCompat.Builder(this, channelId)
                .setContentTitle("타이머 실행 중")
                .setContentText("현재 타이머가 실행 중입니다.")
                .setSmallIcon(R.drawable.ic_timer)
                .setPriority(NotificationCompat.PRIORITY_LOW) // 낮은 우선순위
                .build();

        // 포그라운드 서비스로 시작
        startForeground(1, notification);
    }


}
