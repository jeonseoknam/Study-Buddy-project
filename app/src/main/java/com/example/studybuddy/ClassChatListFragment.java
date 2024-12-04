package com.example.studybuddy;

import static java.lang.Thread.sleep;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.example.studybuddy.databinding.FragmentClassChatListBinding;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabItem;
import com.google.android.material.tabs.TabLayout;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.AggregateQuerySnapshot;
import com.google.firebase.firestore.AggregateSource;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ClassChatListFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ClassChatListFragment extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private final int SINGLE_CHAT = 0, GROUP_CHAT = 1;

    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    ChatListAdapter adapter;

    private SharedPreferences chatNamePref, userPref;
    private RecyclerView recyclerView;
    private String chatAttribute = "공개 채팅방";

    ArrayList<ChatListItem> chatListItems = new ArrayList<>();
    ArrayList<ChatListItem> searchListItems = new ArrayList<>();

    CollectionReference colRef;

    public ClassChatListFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment ClassChatListFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static ClassChatListFragment newInstance(String param1, String param2) {
        ClassChatListFragment fragment = new ClassChatListFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_class_chat_list, container, false);

        userPref = getContext().getSharedPreferences("userData",Context.MODE_PRIVATE);
        colRef = db.collection(userPref.getString("School","none")).document("chat").collection("chatRoom");
        loadChatList();
        // Inflate the layout for this fragment
        return view;
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {

        recyclerView = view.findViewById(R.id.chatListRecyclerView);
        chatNamePref = getContext().getSharedPreferences("chatName", Context.MODE_PRIVATE);
        userPref = getContext().getSharedPreferences("userData",Context.MODE_PRIVATE);

        adapter = new ChatListAdapter(chatListItems);
        recyclerView.setAdapter(adapter);

        TabItem item1 = view.findViewById(R.id.btn_openChat);
        TabItem item2 = view.findViewById(R.id.btn_privateChat);

        TabLayout tabLayout = view.findViewById(R.id.chatTab);
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                Log.d("logchk", "onTabSelected: " + tab.getText().toString());
                chatAttribute = tab.getText().toString();
                chatListItems.clear();
                if (chatAttribute.equals("공개 채팅방")) loadChatList();
                else if (chatAttribute.equals("비공개 채팅방")) loadPrivateChatList();
            }
            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }
            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });

        FloatingActionButton addChatButton = view.findViewById(R.id.addChatButton);
        addChatButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                requireActivity().getSupportFragmentManager().beginTransaction()
                        .add(R.id.fragment_container, CreateChatFragment.newInstance("param1","param2"))
                        .commit();
            }
        });

        EditText chatNameEdittext = view.findViewById(R.id.chatSearchEdittext);
        chatNameEdittext.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                String chatSearch = chatNameEdittext.getText().toString();
                searchListItems.clear();
                if (chatSearch.equals("")){
                    adapter.setListItems(chatListItems);
                } else {
                    for (int i = 0; i < chatListItems.size(); i++){
                        if (chatListItems.get(i).getName().toLowerCase().contains(chatSearch.toLowerCase())){
                            searchListItems.add(chatListItems.get(i));
                        }
                        adapter.setListItems(searchListItems);
                    }
                }
            }
        });

        Button searchButton = view.findViewById(R.id.btn_chatSearch);
        searchButton.setOnClickListener(view1 -> {
            FragmentChatDialog dialog = new FragmentChatDialog();
            dialog.show(getActivity().getSupportFragmentManager(),"tag");

        });

        super.onViewCreated(view, savedInstanceState);
    }
    private void loadChatList(){
        colRef.document("singleChat").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                Map<String, Object> data = value.getData();
                if (value.exists()){
                    List<String> list = new ArrayList<>(data.keySet());
                    list.sort(new Comparator<String>() {
                                  @Override
                                  public int compare(String o1, String o2) {
                                      return data.get(o2).toString().compareTo(data.get(o1).toString());
                                  }
                              }
                    );
                    chatListItems.clear();
                    for (int i = 0 ; i < list.size() ; i++){
                        chatListItems.add(new ChatListItem("","","",""));
                    }
                    for (int sequence = 0; sequence < list.size() ; sequence++){
                        String chatList = list.get(sequence);
                        int finalSequence = sequence;
                        colRef.document("singleChat").collection(chatList).document("msg"+data.get(chatList)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    if (document.exists()) {
                                        Map<String, Object> list = document.getData();
                                        String chatName = chatList;
                                        String message = list.get("message").toString();
                                        String time = list.get("time").toString();
                                        String profile = list.get("profileUrl").toString();
                                        ChatListItem item = new ChatListItem(chatName, message, time, profile);

                                        chatListItems.remove(finalSequence);
                                        chatListItems.add(finalSequence,item);
                                        adapter.notifyDataSetChanged();
                                        //           adapter.notifyItemInserted(chatListItems.size() - 1);
                                        recyclerView.scrollToPosition(0);
                                        Log.d("logchk", "onComplete: " + chatListItems.size());

                                    } else {
                                        Log.d("logchk", "No such document");
                                    }
                                } else {
                                    Log.d("logchk", "get failed with ", task.getException());
                                }
                            }
                        });
                    }
                }
            }
        });
    }
    private void loadPrivateChatList(){
        colRef.document("privateChat").addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot value, @Nullable FirebaseFirestoreException error) {
                Map<String, Object> data = value.getData();
                if (value.exists()){
                    List<String> list = new ArrayList<>(data.keySet());
                    list.sort(new Comparator<String>() {
                                  @Override
                                  public int compare(String o1, String o2) {
                                      return data.get(o2).toString().compareTo(data.get(o1).toString());
                                  }
                              }
                    );
                    chatListItems.clear();
                    for (int i = 0 ; i < list.size() ; i++){
                        chatListItems.add(new ChatListItem("","","",""));
                    }
                    List<String> itemsize = new ArrayList<>();
                    for (int sequence = 0; sequence < list.size() ; sequence++){
                        String chatList = list.get(sequence);
                        int finalSequence = sequence;
                        colRef.document("privateChat").collection(chatList).document("chatSetting").collection("setting").document("user").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                //Log.d("logchk", "onComplete: " + task.getResult().getData());
                                Map<String, Object> fdata = task.getResult().getData();
                                if (fdata.getOrDefault(userPref.getString("UID","none"),"none").equals(userPref.getString("UID","none"))){
                                    itemsize.add(chatList);
                                    colRef.document("privateChat").collection(chatList)
                                            .document("msg"+data.get(chatList)).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                                                @Override
                                                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                                    if (task.isSuccessful()) {
                                                        DocumentSnapshot document = task.getResult();
                                                        Log.d("logchk", "onComplete: " + document.getData());
                                                        if (document.exists()) {
                                                            Map<String, Object> flist = document.getData();
                                                            String chatName = chatList;
                                                            String message = flist.get("message").toString();
                                                            String time = flist.get("time").toString();
                                                            String profile = flist.get("profileUrl").toString();
                                                            ChatListItem item = new ChatListItem(chatName, message, time, profile);
                                                            chatListItems.add(finalSequence,item);
                                                            Log.d("logchk", "onComplete: "+chatListItems.size());
                                                            Log.d("logchk", "onComplete: " + itemsize.size());
                                                            if (chatListItems.size() == list.size() + itemsize.size()){
                                                                for (int i = 0; i < list.size() ; i++){
                                                                    for (int j= 0 ; j <chatListItems.size() ; j++){
                                                                        if (chatListItems.get(j).getName().equals("")){
                                                                            chatListItems.remove(j);
                                                                            break;
                                                                        }
                                                                    }
                                                                }
                                                            }
                                                            adapter.notifyDataSetChanged();

                                                        } else {
                                                            Log.d("logchk", "No such document");
                                                        }
                                                    }
                                                }
                                            });

                                }
                            }
                        });
                    }
                }
            }
        });
    }


    private class MyViewHolder extends RecyclerView.ViewHolder {
        ImageView profile;
        TextView chatname;
        TextView message;
        TextView time;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            profile = itemView.findViewById(R.id.chat_profile);
            chatname = itemView.findViewById(R.id.chat_title);
            message = itemView.findViewById(R.id.chat_subtitle);
            time = itemView.findViewById(R.id.chat_time);
        }
    }

    private class ChatListAdapter extends RecyclerView.Adapter<MyViewHolder> {
        private ArrayList<ChatListItem> listItems;

        private ChatListAdapter(ArrayList<ChatListItem> chatListItem) {
            this.listItems = chatListItem;
        }

        @Override
        public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
            ChatListItem item = listItems.get(position);

            holder.chatname.setText(item.chatname);
            holder.message.setText(item.message);
            holder.time.setText(item.time);
            if (item.profileUrl != null)
                Glide.with(getActivity()).load(item.profileUrl).into(holder.profile);
            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    SharedPreferences.Editor editor = chatNamePref.edit();
                    editor.putString("Name", item.chatname);
                    if (chatAttribute.equals("공개 채팅방"))
                        editor.putString("open","singleChat");
                    else if (chatAttribute.equals("비공개 채팅방"))
                        editor.putString("open","privateChat");
                    editor.apply();
                    requireActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, new ChatRoomFragment())
                            .commit();
                }
            });
        }

        @Override
        public int getItemCount() {
            return listItems.size();
        }

        public void setListItems(ArrayList<ChatListItem> chatListItems){
            this.listItems = chatListItems;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.chat_recycler_view, parent, false);
            return new MyViewHolder(itemView);
        }
    }
}