package com.example.studybuddy;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.input.InputManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.databinding.ActivityChatroomBinding;
import com.example.studybuddy.utility.userData;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class ChatroomActivity extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private String chatname;
    private final int MY_CHAT=1, OTHER_CHAT=0;
    ChatAdapter adapter;
    private SharedPreferences userPref = getSharedPreferences("userData", Context.MODE_PRIVATE);
    ArrayList<ChatMessageItem> messageItems = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        ActivityChatroomBinding binding = ActivityChatroomBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        Intent getdataIntent = getIntent();
        chatname = getdataIntent.getStringExtra("chatname");

        binding.chatTitleText.setText(chatname);

        adapter = new ChatAdapter(messageItems);
        binding.chatRecyclerView.setAdapter(adapter);

        final CollectionReference chatRef = db.collection("chatRoom").document("singleChat").collection(chatname);
        chatRef.addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException error) {
                List<DocumentChange> documentChanges = value.getDocumentChanges();
                for (DocumentChange documentChange : documentChanges){
                    if (error != null) {
                        Log.w("logchk", "Listen failed.", error);
                        return;
                    }
                    if (value != null && !value.isEmpty()) {
                        DocumentSnapshot snapshot = documentChange.getDocument();
                        if (snapshot.equals(value.getDocuments().get(0)))
                            Log.d("logchk", "onEvent: right");
                        else {
                            Map<String, Object> msg = snapshot.getData();
                            String name = msg.get("name").toString();
                            String message = msg.get("message").toString();
                            String time = msg.get("time").toString();
                            String profileUrl = msg.get("profileUrl").toString();

                            ChatMessageItem item = new ChatMessageItem(name, message, time, profileUrl);

                            messageItems.add(item);
                            adapter.notifyItemInserted(messageItems.size() - 1);
                            binding.chatRecyclerView.scrollToPosition(messageItems.size() - 1);
                            Log.d("logchk", "onEvent: " + messageItems);
                        }
                    } else {
                        Log.d("logchk", "Current data: null");
                    }
                }
            }
        });
        binding.btnSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = userPref.getString("Nickname", "none");
                String message = binding.messageInput.getText().toString();
                String profileUrl = userPref.getString("Profile", null);
                Calendar calendar = Calendar.getInstance();
                String time = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
                Object currentTime = System.currentTimeMillis();
                Map<String, Object> lastTime = new HashMap<>();
                lastTime.put(chatname, currentTime);
                db.collection("chatRoom").document("singleChat").update(lastTime);
                ChatMessageItem item = new ChatMessageItem(nickname, message, time, profileUrl);
                chatRef.document("msg" + currentTime).set(item);
                binding.messageInput.setText("");

                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getCurrentFocus().getWindowToken(), 0);
            }
        });

        binding.btnChatMenu.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ChatroomActivity.this, ChatRoomMenuActivity.class);
                intent.putExtra("chatname", chatname);
                startActivity(intent);
            }
        });
    }

    private class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView profile;
        TextView name;
        TextView message;
        TextView time;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.profileImage);
            name = itemView.findViewById(R.id.username);
            message = itemView.findViewById(R.id.messageText);
            time = itemView.findViewById(R.id.chatMessage_time);
        }
    }

    private class ChatAdapter extends RecyclerView.Adapter<MyViewHolder>{
        private ArrayList<ChatMessageItem> messageItems;

        private ChatAdapter(ArrayList<ChatMessageItem> messageItems){
            this.messageItems = messageItems;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            ChatMessageItem item = messageItems.get(position);

            holder.name.setText(item.name);
            holder.message.setText(item.message);
            holder.time.setText(item.time);
            if (!item.profileUrl.equals("none"))
                Glide.with(ChatroomActivity.this).load(item.profileUrl).into(holder.profile);
        }

        @Override
        public int getItemViewType(int position) {
            if (messageItems.get(position).name.equals(userPref.getString("Nickname","none"))){
                return  MY_CHAT;
            }else { return OTHER_CHAT; }
        }

        @Override
        public int getItemCount() {
            return messageItems.size();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = null;
            if (viewType == MY_CHAT){
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_my_chat_message,parent,false);
            } else {
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_chat_message, parent, false);
            }
            return new MyViewHolder(itemView);
        }
    }

}