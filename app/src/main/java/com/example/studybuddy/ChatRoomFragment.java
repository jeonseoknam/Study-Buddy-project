package com.example.studybuddy;

import static androidx.core.content.ContextCompat.getSystemService;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomFragment extends Fragment {

    private FirebaseFirestore db;
    private String chatname;
    private String testname;

    private SharedPreferences userPref;
    private final int MY_CHAT=1, OTHER_CHAT=0;
    ChatAdapter adapter;
    ArrayList<ChatMessageItem> messageItems = new ArrayList<>();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_room, container, false);
        TextView chatTitle = view.findViewById(R.id.chatTitleText);

        chatname = ClassChatListFragment.chatName;
        Log.d("logchk", "onCreateView: "+ ClassChatListFragment.chatName);
        chatTitle.setText(chatname);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        db = FirebaseFirestore.getInstance();
        userPref = getContext().getSharedPreferences("userData", Context.MODE_PRIVATE);

        RecyclerView recyclerView = view.findViewById(R.id.chatRecyclerView);
        adapter = new ChatAdapter(messageItems);
        recyclerView.setAdapter(adapter);

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
                            recyclerView.scrollToPosition(messageItems.size() - 1);
                        }
                    } else {
                        Log.d("logchk", "Current data: null");
                    }
                }
            }
        });

        ImageButton sendButton = view.findViewById(R.id.btn_sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = userPref.getString("Nickname","none");
                EditText messageInput = view.findViewById(R.id.messageInput);
                String message = messageInput.getText().toString();
                if (!message.isEmpty()){
                    String profileUrl = userPref.getString("Profile",null);
                    Calendar calendar = Calendar.getInstance();
                    String time = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
                    Object currentTime = System.currentTimeMillis();
                    Map<String, Object> lastTime = new HashMap<>();
                    lastTime.put(chatname, currentTime);
                    db.collection("chatRoom").document("singleChat").update(lastTime);
                    ChatMessageItem item = new ChatMessageItem(nickname, message, time, profileUrl);
                    chatRef.document("msg" + currentTime).set(item);
                    messageInput.setText("");

                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED,InputMethodManager.HIDE_NOT_ALWAYS);
                }
            }
        });

        ImageButton chatMenuButton = view.findViewById(R.id.btn_chatMenu);
        chatMenuButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getParentFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, new ChatRoomMenuFragment())
                        .addToBackStack(null)
                        .commit();
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

    private class ChatAdapter extends RecyclerView.Adapter<ChatRoomFragment.MyViewHolder>{
        private ArrayList<ChatMessageItem> messageItems;

        private ChatAdapter(ArrayList<ChatMessageItem> messageItems){
            this.messageItems = messageItems;
        }

        @Override
        public void onBindViewHolder(@NonNull ChatRoomFragment.MyViewHolder holder, int position) {
            ChatMessageItem item = messageItems.get(position);

            holder.name.setText(item.name);
            holder.message.setText(item.message);
            holder.time.setText(item.time);
            if (!item.profileUrl.equals("none"))
                Glide.with(ChatRoomFragment.this).load(item.profileUrl).into(holder.profile);
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
        public ChatRoomFragment.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = null;
            if (viewType == MY_CHAT){
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_my_chat_message,parent,false);
            } else {
                itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.activity_chat_message, parent, false);
            }
            return new ChatRoomFragment.MyViewHolder(itemView);
        }
    }

}
