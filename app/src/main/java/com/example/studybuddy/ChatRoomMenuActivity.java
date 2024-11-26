package com.example.studybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
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

    private String chatID;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chat_room_menu);

        Intent intent = getIntent();
        chatID = intent.getStringExtra("chatname");

        if (chatID != null) {
            Log.d("ChatRoomMenuTag", "ReceiveChatID = " + chatID);
        } else {
            Log.d("ChatRoomMenuTag", "No chatID received!");
        }

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

        Button btn_goalBoard = findViewById(R.id.btn_goalBoard);
        btn_goalBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoalBoardFragment fragment = new GoalBoardFragment();

                Bundle args = new Bundle();
                args.putString("chatID", chatID);
                fragment.setArguments(args);

                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });


    }
}