package com.example.studybuddy;

import static android.app.Activity.RESULT_OK;
import static androidx.core.content.ContextCompat.getSystemService;

import static java.lang.Thread.sleep;

import android.animation.Animator;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Dimension;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.databinding.FragmentChatRoomBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChatRoomFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private FirebaseStorage storage;
    private Uri fileUri = null;
    private String chatname, chatOpen, chatnameset;
    private ImageView addFile, expend, totalView;
    private CollectionReference chatRef;

    private SharedPreferences userPref, chatNamePref;
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
        chatNamePref = getContext().getSharedPreferences("chatName", Context.MODE_PRIVATE);

        chatname = chatNamePref.getString("Name","none");
        chatOpen = chatNamePref.getString("open","singleChat");
        Log.d("logchk", "onCreateView: "+ chatname);
        chatTitle.setText(chatname);

        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
        storage = FirebaseStorage.getInstance();
        userPref = getContext().getSharedPreferences("userData", Context.MODE_PRIVATE);

        totalView = view.findViewById(R.id.total_image);
        expend = view.findViewById(R.id.expanded_image);
        expend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (expend.getVisibility() == View.VISIBLE){
                    expend.setVisibility(View.GONE);
                    totalView.setVisibility(View.GONE);
                    Log.d("logchk", "onClick: " + expend.getVisibility());
                    expend.setImageResource(0);
                }
            }
        });
        chatRef = db.collection(userPref.getString("School","none")).document("chat").collection("chatRoom").document(chatOpen).collection(chatname);
        chatRef.document("chatSetting").collection("setting")
                .document("setting").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        chatnameset = task.getResult().getData().get("name").toString();
                    }
                });

        RecyclerView recyclerView = view.findViewById(R.id.chatRecyclerView);
        try{
            sleep(1000);
        } catch (InterruptedException e){

        }
        adapter = new ChatAdapter(messageItems);
        recyclerView.setAdapter(adapter);


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
                        Map<String, Object> msg = snapshot.getData();
                        String name = (String) msg.get("name");
                        String message = (String) msg.get("message");
                        String time = (String) msg.get("time");
                        String profileUrl = (String) msg.get("profileUrl");
                        String imageMessage = (String) msg.get("imageMessage");

                        ChatMessageItem item = new ChatMessageItem(name, message, time, profileUrl,imageMessage);

                        messageItems.add(item);
                        adapter.notifyItemInserted(messageItems.size() - 1);
                        recyclerView.scrollToPosition(messageItems.size() - 1);

                    } else {
                        Log.d("logchk", "Current data: null");
                    }
                }
            }
        });

        addFile = view.findViewById(R.id.addImageFile);
        TextView deleteFile = view.findViewById(R.id.btn_fileDeleteButton);
        ImageButton addFileButton = view.findViewById(R.id.btn_attachButton);
        addFileButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK);
                intent.setDataAndType(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, "image/*");
                intent.setAction(Intent.ACTION_PICK);
                fileLauncher.launch(intent);
                ViewGroup.LayoutParams params = addFile.getLayoutParams();
                deleteFile.setTextSize(Dimension.DP, 60);
                params.height = 500;
                addFile.setLayoutParams(params);
            }
        });
        deleteFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ViewGroup.LayoutParams params = addFile.getLayoutParams();
                deleteFile.setTextSize(Dimension.DP, 1);
                params.height = 1;
                addFile.setLayoutParams(params);
                addFile.setImageResource(0);
                fileUri = null;
            }
        });

        ImageButton sendButton = view.findViewById(R.id.btn_sendButton);
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String anickname = userPref.getString("Nickname","none");
                if (chatnameset.equals("realName")){
                    anickname = userPref.getString("Name","none");
                }
                final String nickname = anickname;
                EditText messageInput = view.findViewById(R.id.messageInput);
                String message = messageInput.getText().toString();
                Log.d("logchk", "onClick: "+fileUri);
                String imageMessage = null;
                Object currentTime = System.currentTimeMillis();
                if (!message.isEmpty()){
                    if (fileUri != null) {
                        StorageReference stoRef = storage.getReference("chatImage/" + System.currentTimeMillis());
                        stoRef.putFile(fileUri);
                        try {
                            sleep(3000);
                        } catch (InterruptedException e){
                            Log.d("logchk", "onClick: " + e);
                        }
                        stoRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                            @Override
                            public void onSuccess(Uri uri) {
                                String imageMessage = uri.toString();
                                fileUri = null;
                                String profileUrl = userPref.getString("Profile", null);
                                Calendar calendar = Calendar.getInstance();
                                String time = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
                                Map<String, Object> lastTime = new HashMap<>();
                                lastTime.put(chatname, currentTime);
                                db.collection(userPref.getString("School","none")).document("chat").collection("chatRoom").document(chatOpen).update(lastTime);
                                ChatMessageItem item = new ChatMessageItem(nickname, message, time, profileUrl, imageMessage);
                                chatRef.document("msg" + currentTime).set(item);
                            }
                        }).addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                Log.d("logchk", "onFailure: 다운 실패" + e);
                            }
                        });
                    } else {
                        String profileUrl = userPref.getString("Profile", null);
                        Calendar calendar = Calendar.getInstance();
                        String time = calendar.get(Calendar.HOUR_OF_DAY) + ":" + calendar.get(Calendar.MINUTE);
                        Map<String, Object> lastTime = new HashMap<>();
                        lastTime.put(chatname, currentTime);
                        db.collection(userPref.getString("School","none")).document("chat").collection("chatRoom").document(chatOpen).update(lastTime);
                        ChatMessageItem item = new ChatMessageItem(nickname, message, time, profileUrl, imageMessage);
                        chatRef.document("msg" + currentTime).set(item);
                    }
                    messageInput.setText("");
                    ViewGroup.LayoutParams params = addFile.getLayoutParams();
                    deleteFile.setTextSize(Dimension.DP, 1);
                    params.height = 1;
                    addFile.setLayoutParams(params);
                    addFile.setImageResource(0);

                    InputMethodManager imm = (InputMethodManager) getContext().getSystemService(Context.INPUT_METHOD_SERVICE);
                    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_NOT_ALWAYS);
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
    private ActivityResultLauncher<Intent> fileLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult o) {
                    if (o.getResultCode() == RESULT_OK){
                        Intent intent = o.getData();
                        fileUri = intent.getData();
                        Glide.with(addFile).load(fileUri).into(addFile);
                        Map<String, Object> test = new HashMap<>();
                        test.put("link", fileUri);
                        db.collection("chatReferences").document("item"+System.currentTimeMillis()).set(test);
                    }
                }
            }
    );




    private class MyViewHolder extends RecyclerView.ViewHolder{
        ImageView profile;
        ImageView imageMessage;
        TextView name;
        TextView message;
        TextView time;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.profileImage);
            imageMessage = itemView.findViewById(R.id.messageImage);
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
            if (item.imageMessage != null){
                ViewGroup.LayoutParams params = holder.imageMessage.getLayoutParams();
                params.width = 500;
                Glide.with(ChatRoomFragment.this).load(item.imageMessage).into(holder.imageMessage);
            }
            if (item.profileUrl != null)
                Glide.with(ChatRoomFragment.this).load(item.profileUrl).into(holder.profile);
            if (item.imageMessage != null){
                holder.imageMessage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        expend.setVisibility(View.VISIBLE);
                        totalView.setVisibility(View.VISIBLE);
                        Glide.with(ChatRoomFragment.this).load(item.imageMessage).into(expend);
                        Log.d("logchk", "onClick: " + expend.getVisibility());
                    }
                });
            }
        }

        @Override
        public int getItemViewType(int position) {
            if (chatnameset.equals("anonymous")){
                if (messageItems.get(position).name.equals(userPref.getString("Nickname","none"))){
                    return  MY_CHAT;
                }else { return OTHER_CHAT; }
            }else {
                if (messageItems.get(position).name.equals(userPref.getString("Name","none"))){
                    return  MY_CHAT;
                }else { return OTHER_CHAT; }
            }
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