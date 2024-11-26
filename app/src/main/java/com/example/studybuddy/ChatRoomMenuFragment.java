package com.example.studybuddy;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class ChatRoomMenuFragment extends Fragment {

    private String chatID;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_room_menu,container,false);

        chatID = ClassChatListFragment.chatName;

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView chatTitle = view.findViewById(R.id.chatTitleText);
        chatTitle.setText(chatID);

        Button btn_calendar = view.findViewById(R.id.btn_classCalendar);
        btn_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ChatCalendarActivity.class);
                startActivity(intent);
            }
        });

        Button btn_goalBoard = view.findViewById(R.id.btn_goalBoard);
        btn_goalBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoalBoardFragment fragment = new GoalBoardFragment();

                Bundle args = new Bundle();
                args.putString("chatID", chatID);
                fragment.setArguments(args);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

    }

}
