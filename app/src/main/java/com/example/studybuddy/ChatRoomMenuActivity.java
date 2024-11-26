package com.example.studybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

public class ChatRoomMenuActivity extends AppCompatActivity {



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_menu);

        Button btn_calendar = findViewById(R.id.btn_classCalendar);
        btn_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                btn_calendar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(ChatRoomMenuActivity.this, ChatCalendarActivity.class);
                        startActivity(intent);

                    }
                });



            }
        });
/*
        Button btn_goalBoard = findViewById(R.id.btn_goalBoard);
        btn_goalBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatRoomMenuActivity.this, GoalBoardFragment.class);
                intent.putExtra("chatRoomId", chatRoomId);  // 현재 채팅방 ID 전달
                startActivity(intent);
            }
        });

 */
    }
}