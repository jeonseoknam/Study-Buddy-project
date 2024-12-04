package com.example.studybuddy;

import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

public class ChatRoomMenuFragment extends Fragment {

    private String chatID;
    private String nickName, chatOpen, chatCode = "";
    private SharedPreferences userPref;
    private SharedPreferences chatNamePref;
    private FirebaseFirestore db;
    private Button chatCodeButton;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_room_menu,container,false);
        db = FirebaseFirestore.getInstance();
        chatNamePref = getContext().getSharedPreferences("chatName", Context.MODE_PRIVATE);
        chatID = chatNamePref.getString("Name", "none");
        chatOpen = chatNamePref.getString("open","singleChat");
        userPref = getContext().getSharedPreferences("userData", Context.MODE_PRIVATE);
        nickName = userPref.getString("Nickname", "none");

        chatCodeButton = view.findViewById(R.id.btn_Invite);

        if (chatOpen.equals("privateChat")) {
            CollectionReference chatRef = db.collection(userPref.getString("School","none")).document("chat").collection("chatRoom").document(chatOpen).collection(chatID);
            chatRef.document("chatSetting").collection("setting")
                    .document("setting").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                            chatCode = task.getResult().getData().get("chatCode").toString();
                            chatCodeButton.setText("초대코드 : " + chatCode);
                        }
                    });
        } else if (chatOpen.equals("singleChat")) {
            chatCodeButton.setVisibility(View.GONE);
        }

        return view;

    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        ImageButton backbutton = view.findViewById(R.id.btn_backButton);
        backbutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .remove(ChatRoomMenuFragment.this)
                        .commit();
            }
        });

        TextView chatTitle = view.findViewById(R.id.chatTitleText);
        chatTitle.setText(chatID);

        ClipboardManager clipboard = (ClipboardManager) getContext().getSystemService(Context.CLIPBOARD_SERVICE);
        chatCodeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ClipData clip = ClipData.newPlainText("text",chatCode);
                clipboard.setPrimaryClip(clip);
                Toast.makeText(getContext(), clip + "이 복사되었습니다.", Toast.LENGTH_SHORT).show();
            }
        });

        Button btn_calendar = view.findViewById(R.id.btn_classCalendar);
        btn_calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), ChatCalendarActivity.class);
                intent.putExtra("chatRoomId", chatID);
                startActivity(intent);
            }
        });

        Button btn_classTimer = view.findViewById(R.id.btn_classTimer);
        btn_classTimer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                ChatTimerFragment fragment = new ChatTimerFragment();
                Bundle args = new Bundle();
                args.putString("chatRoomId", chatID);
                args.putString("nickname", nickName);
                fragment.setArguments(args);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

        Button btn_goalBoard = view.findViewById(R.id.btn_goalBoard);
        btn_goalBoard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                GoalBoardFragment fragment = new GoalBoardFragment();

                Bundle args = new Bundle();
                args.putString("chatRoomId", chatID);
                args.putString("nickname", nickName);
                fragment.setArguments(args);

                getParentFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container, fragment)
                        .addToBackStack(null)
                        .commit();
            }
        });

    }

}
